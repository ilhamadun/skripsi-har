from abc import ABC, abstractmethod
import io
import itertools
import math
import matplotlib.pyplot as plt
import numpy as np
import tensorflow as tf
from data import iterator

class BaseModel(ABC):
    """Base class for HAR architecture"""

    def __init__(self):
        self.session = None

        # Training placeholders
        self.input = tf.placeholder(tf.float32, [None, 600], name='input')
        self.target = tf.placeholder(tf.float32, [None, 6], name='target')
        self.is_training = tf.placeholder(tf.bool, name='is_training')

        self.output, self.train_op, self.accuracy_op = None, None, None
        self.saver = None

    @abstractmethod
    def build_graph(self):
        pass

    def magnitude(self):
        with tf.name_scope('magnitude'):
            reshapred_input = tf.reshape(self.input, [-1, 100, 6])
            accelerometer, gyroscope = tf.split(reshapred_input, 2, 2)

            accelerometer = self._append_norm(accelerometer, 'accelerometer')
            gyroscope = self._append_norm(gyroscope, 'gyroscope')

            return tf.concat([accelerometer, gyroscope], 2)

    def _append_norm(self, tensor_in, name_scope):
        with tf.name_scope(name_scope):
            norm = tf.norm(tensor_in, axis=2)
            norm = tf.reshape(norm, [-1, 100, 1])

            return tf.concat([tensor_in, norm], 2)

    def create_train_op(self, learning_rate):
        loss = tf.losses.softmax_cross_entropy(self.target, self.output, scope='loss')

        with tf.name_scope('optimizer'):
            optimizer = tf.train.RMSPropOptimizer(learning_rate)
            return optimizer.minimize(loss)

    def create_accuracy_op(self):
        with tf.name_scope('accuracy'):
            with tf.name_scope('correct_prediction'):
                correct_prediction = tf.equal(tf.argmax(self.output, 1), tf.argmax(self.target, 1))

            with tf.name_scope('accuracy'):
                accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
                tf.summary.scalar('accuracy', accuracy)

        return accuracy

    def train(self, train, validation, epoch, batch_size, checkpoint=None):
        data_length = train.data.shape[0]
        data_iterator = iterator(train.data, train.target, data_length, batch_size)
        merged_summary = tf.summary.merge_all()
        best_accuracy, best_step = 0, 0

        number_of_step = self._epoch_to_step(epoch, data_length, batch_size)
        one_epoch_step = self._epoch_to_step(1, data_length, batch_size)

        if checkpoint is not None:
            self.saver.restore_checkpoint(checkpoint)

        self.saver.save_graph_def('graph.pbtxt')

        print('Training %d data in %d steps...' % (data_length, number_of_step))

        for step in range(number_of_step):
            batch_features, batch_target = next(data_iterator)

            if step % one_epoch_step == 0 or step == number_of_step - 1:
                accuracy = self._evaluate(validation, merged_summary, step)
                best_accuracy, best_step = self._compare_accuracy(
                    accuracy, best_accuracy, step, best_step)
                self.saver.save_checkpoint('last', step)

                early_stop = self._early_stop(
                    self._step_to_epoch(step, data_length, batch_size),
                    self._step_to_epoch(best_step, data_length, batch_size),
                    10
                )

                if early_stop:
                    break

            summary, _ = self.session.run([merged_summary, self.train_op], {
                self.input: batch_features,
                self.target: batch_target.eval(),
                self.is_training: True
            })
            self.saver.save_summary('train', summary, step)

    def _evaluate(self, validation, merged_summary, step):
        summary, accuracy = self.session.run([merged_summary, self.accuracy_op], {
            self.input: validation.data,
            self.target: validation.target.eval(),
            self.is_training: False
        })

        self.saver.save_summary('test', summary, step)

        print('Step %d: %f' % (step, accuracy))

        return accuracy

    def _compare_accuracy(self, last_accuracy, best_accuracy, step, best_accuracy_step):
        if last_accuracy > best_accuracy:
            best_accuracy = last_accuracy
            best_accuracy_step = step
            self.saver.save_checkpoint('best', step)

        return best_accuracy, best_accuracy_step

    def _early_stop(self, last_accuracy_epoch, best_accuracy_epoch, limit):
        return (last_accuracy_epoch - best_accuracy_epoch) >= limit

    def _epoch_to_step(self, epoch, data_length, batch_size):
        step_per_epoch = math.ceil(data_length / batch_size)
        number_of_steps = int(step_per_epoch * epoch)

        return number_of_steps

    def _step_to_epoch(self, step, data_length, batch_size):
        step_per_epoch = math.ceil(data_length / batch_size)
        epoch = math.floor(step / step_per_epoch)

        return epoch

    def test(self, test_data, batch_size, checkpoint=None):
        data_length = test_data.data.shape[0]
        data_iterator = iterator(test_data.data, test_data.target, data_length, batch_size)
        number_of_steps = self._epoch_to_step(1, data_length, batch_size)
        all_prediction = None
        accuracy_sum = 0

        if checkpoint is None:
            checkpoint = self.saver.checkpoint_path['best']

        self.saver.restore_checkpoint(checkpoint)

        print('Testing %d sample in %d steps...' % (data_length, number_of_steps))

        for step in range(number_of_steps):
            batch_features, batch_target = next(data_iterator)

            prediction, accuracy = self.session.run([
                self.output, self.accuracy_op], feed_dict={
                    self.input: batch_features,
                    self.target: batch_target.eval(),
                    self.is_training: False
                })
            accuracy_sum += accuracy

            all_prediction = self._merge_prediction(all_prediction, prediction)

        total_accuracy = accuracy_sum / number_of_steps
        print('Total accuracy: %g' % total_accuracy)

        return all_prediction

    def _merge_prediction(self, all_prediction, prediction):
        if all_prediction is None:
            all_prediction = prediction
        else:
            all_prediction = tf.concat([all_prediction, prediction], axis=0)

        return all_prediction

    def confusion_matrix(self, predictions, labels):
        predictions = tf.argmax(predictions, 1)
        labels = tf.argmax(labels, 1)
        cm_values = tf.confusion_matrix(labels, predictions).eval()
        cm_values = np.array(cm_values, dtype=np.float32)

        normalized_cm = (cm_values / np.sum(cm_values, axis=1)[:, np.newaxis]) * 100

        activity_label = [
            'BERDIRI',
            'DUDUK',
            'JALAN',
            'LARI',
            'NAIK TANGGA',
            'TURUN TANGGA',
        ]

        plt.imshow(normalized_cm, interpolation='nearest', cmap=plt.cm.Blues)
        plt.colorbar()
        tick_marks = np.arange(6)
        plt.xticks(tick_marks, activity_label, fontsize='x-small')
        plt.yticks(tick_marks, activity_label, fontsize='x-small')
        plt.ylabel('Label')
        plt.xlabel('Prediksi')

        thresh = normalized_cm.max() / 2
        for i, j in itertools.product(range(normalized_cm.shape[0]),
                                      range(normalized_cm.shape[1])):
            plt.text(j, i, '%.1f' % (normalized_cm[i, j]),
                     horizontalalignment="center",
                     color="white" if normalized_cm[i, j] > thresh else "black",
                     fontsize='x-small')

        buffer = io.BytesIO()
        plt.savefig(buffer, dpi=150, bbox_inches='tight', pad_inches=0.3, format='png')

        image = tf.image.decode_png(buffer.getvalue(), channels=4)
        image = tf.expand_dims(image, 0)
        image_op = tf.summary.image('Confusion Matrix', image)
        self.saver.log_test.add_summary(self.session.run(image_op))

        plt.clf()
        plt.cla()
        plt.close()
