import io
import itertools
import glob
import math
import os
import fire
import numpy as np
import tensorflow as tf
import tensorflow.contrib.slim as slim
import matplotlib.pyplot as plt
import data


class HARConvLSTM:
    def __init__(self):
        self.session = tf.Session()
        self.log_train, self.log_test = None, None
        self.best_directory, self.last_directory = None, None
        self.checkpoint_path, self.best_checkpoint_path = None, None
        self.features = tf.placeholder(tf.float32, [None, 600], name='input')
        self.target = tf.placeholder(tf.float32, [None, 7], name='target')
        self.keep_prob = tf.placeholder(tf.float32, name='keep_prob')

        features = self.__preprocessing()
        conv1_input = tf.reshape(features, [-1, 100, 2, 4])
        conv1 = self.__conv_layer(conv1_input, [5, 2, 4, 32], 'conv1')
        conv2 = self.__conv_layer(conv1, [5, 2, 32, 32], 'conv2')
        lstm_input = tf.reshape(conv2, [-1, 25, 64])
        lstm = self.__lstm_layers(lstm_input, 128, 2)
        self.output = slim.fully_connected(lstm, 7, activation_fn=tf.nn.softmax, scope='output')
        self.train_op, self.accuracy = self.__train_layers(self.output, self.target)

    def __preprocessing(self):
        with tf.name_scope('preprocessing'):
            reshaped_input = tf.reshape(self.features, [-1, 100, 6])

            with tf.name_scope('magnitude'):
                acc, gyr = tf.split(reshaped_input, 2, 2)
                acc = self.__magnitude(acc, 'accelerometer')
                gyr = self.__magnitude(gyr, 'gyroscope')

            return tf.concat([acc, gyr], 2)

    def __magnitude(self, raw_input, name_scope):
        with tf.name_scope(name_scope):
            magnitude = tf.norm(raw_input, axis=2)
            magnitude = tf.reshape(magnitude, [-1, 100, 1])

            return tf.concat([raw_input, magnitude], 2)

    def __conv_layer(self, tensor_in, filters, name_scope):
        with tf.name_scope(name_scope):
            weight = self.__weight_variable(filters)
            bias = self.__bias_variable([filters[3]])

            conv = tf.nn.conv2d(tensor_in, weight, [1, 2, 1, 1], 'SAME')
            conv = tf.nn.bias_add(conv, bias)
            return tf.nn.relu(conv)

    def __weight_variable(self, shape):
        with tf.name_scope('weights'):
            initial = tf.random_normal(shape)
            weights = tf.Variable(initial)
            self.__variable_summaries(weights)

        return weights

    def __bias_variable(self, shape):
        with tf.name_scope('biases'):
            initial = tf.random_normal(shape)
            biases = tf.Variable(initial)
            self.__variable_summaries(biases)

        return biases

    def __lstm_layers(self, tensor_in, num_units, num_layers):
        with tf.name_scope('LSTM'):
            lstm_input = tf.transpose(tensor_in, [1, 0, 2])

            lstm = [tf.contrib.rnn.BasicLSTMCell(num_units) for _ in range(num_layers)]
            lstm_cells = tf.contrib.rnn.MultiRNNCell(lstm)
            lstm_outputs, _ = tf.nn.dynamic_rnn(lstm_cells,
                                                lstm_input,
                                                dtype=tf.float32,
                                                time_major=True)

            return lstm_outputs[-1]

    def __train_layers(self, tensor_in, target):
        loss = tf.losses.softmax_cross_entropy(target, tensor_in, scope='loss')

        with tf.name_scope('optimizer'):
            optimizer = tf.train.RMSPropOptimizer(learning_rate=0.001)
            train_op = optimizer.minimize(loss)

        with tf.name_scope('accuracy'):
            with tf.name_scope('correct_prediction'):
                correct_prediction = tf.equal(tf.argmax(tensor_in, 1), tf.argmax(target, 1))

            with tf.name_scope('accuracy'):
                accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
                tf.summary.scalar('accuracy', accuracy)

        return train_op, accuracy

    def initialize_logs(self, logdir):
        train_path = os.path.join(logdir, 'train')
        test_path = os.path.join(logdir, 'test')

        self.log_train = tf.summary.FileWriter(train_path, graph=tf.get_default_graph())
        self.log_test = tf.summary.FileWriter(test_path, graph=tf.get_default_graph())

    def initialize_checkpoint(self, savedir):
        self.best_directory = os.path.join(savedir, 'best')
        self.last_directory = os.path.join(savedir, 'last')

        if not os.path.exists(self.best_directory):
            os.makedirs(self.best_directory)

        if not os.path.exists(self.last_directory):
            os.makedirs(self.last_directory)

    def __variable_summaries(self, var):
        with tf.name_scope('summaries'):
            mean = tf.reduce_mean(var)
            tf.summary.scalar('mean', mean)

            with tf.name_scope('stddev'):
                stddev = tf.sqrt(tf.reduce_mean(tf.square(var - mean)))

            tf.summary.scalar('stddev', stddev)
            tf.summary.scalar('max', tf.reduce_max(var))
            tf.summary.scalar('min', tf.reduce_min(var))
            tf.summary.histogram('histogram', var)

    def write_graph(self, output):
        with self.session.as_default():
            tf.train.write_graph(self.session.graph.as_graph_def(), self.best_directory, output)

            print('Graph definitions created on', os.path.join(self.best_directory, output))

    def save_checkpoint(self, saver, step):
        checkpoint_prefix = os.path.join(self.last_directory, 'saved_checkpoint')
        self.checkpoint_path = saver.save(self.session, checkpoint_prefix, global_step=step)

    def save_best_checkpoint(self, saver, step):
        checkpoint_prefix = os.path.join(self.best_directory, 'saved_checkpoint')
        self.best_checkpoint_path = saver.save(self.session, checkpoint_prefix, global_step=step)

    def train(self, features, target, batch_size, number_of_steps, load=None):
        merged_summary = tf.summary.merge_all()
        last_saver = tf.train.Saver(max_to_keep=10)
        best_saver = tf.train.Saver(max_to_keep=1)
        train_iterator = data.iterator(features, target, features.shape[0], batch_size)
        best_accuracy = 0

        if load is not None:
            data.load_model(self.session, load)

        self.write_graph('graph.pbtxt')

        print('Training %d data in %d steps...' % (features.shape[0], number_of_steps))

        with self.session.as_default():
            for step in range(number_of_steps):
                batch_features, batch_target = next(train_iterator)

                if step % 10 == 0:
                    summary, accuracy = self.session.run([merged_summary, self.accuracy], {
                        self.features: batch_features,
                        self.target: batch_target.eval(),
                        self.keep_prob: 1.0
                    })
                    self.log_test.add_summary(summary, step)

                    if accuracy >= best_accuracy:
                        best_accuracy = accuracy
                        self.save_best_checkpoint(best_saver, step)
                    else:
                        self.save_checkpoint(last_saver, step)

                    print("step %5d\taccuracy: %5g" % (step, accuracy))

                summary, accuracy = self.session.run([merged_summary, self.train_op], feed_dict={
                    self.features: batch_features,
                    self.target: batch_target.eval(),
                    self.keep_prob: 0.2
                })
                self.log_train.add_summary(summary, step)

    def test(self, features, target, load=None):
        print('Testing %d sample...' % (features.shape[0]))

        if load is None:
            load = self.best_checkpoint_path

        data.load_model(self.session, load)

        with self.session.as_default():
            predictions, accuracy = self.session.run([self.output, self.accuracy], feed_dict={
                self.features: features,
                self.target: target.eval(),
                self.keep_prob: 1.0
            })

            print("Accuracy: %5g" % (accuracy))

        return predictions, accuracy

    def confusion_matrix(self, predictions, labels, logdir):
        predictions = predictions.argmax(1)
        labels = tf.argmax(labels, 1)
        with self.session.as_default():
            cm_values = tf.confusion_matrix(labels, predictions).eval()
            cm_values = np.array(cm_values, dtype=np.float32)

        normalized_cm = (cm_values / np.sum(cm_values, axis=1)[:, np.newaxis]) * 100

        labels = [
            'STANDING',
            'SITTING',
            'WALKING',
            'JOGGING',
            'WALKING_UPSTAIRS',
            'WALKING_DOWNSTAIRS',
            'BIKING',
        ]

        plt.imshow(normalized_cm, interpolation='nearest', cmap=plt.cm.Blues)
        plt.suptitle('Confusion Matrix')
        plt.colorbar()
        tick_marks = np.arange(7)
        plt.xticks(tick_marks, labels, rotation=90, fontsize='x-small')
        plt.yticks(tick_marks, labels, fontsize='x-small')
        plt.ylabel('True label')
        plt.xlabel('Predicted label')

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
        self.log_test.add_summary(self.session.run(image_op))


def epoch_to_steps(epoch, data_length, batch_size):
    step_per_epoch = math.ceil(data_length / batch_size)
    number_of_steps = int(step_per_epoch * epoch)

    return number_of_steps

def main(train_dir=None, test_dir=None, epoch=5, batch_size=100, logdir=None, load=None):
    model = HARConvLSTM()

    if logdir:
        model.initialize_logs(os.path.join(logdir, 'summary'))
        model.initialize_checkpoint(os.path.join(logdir, 'checkpoint'))

    target_num = 7
    window_size = 100

    model.session.run(tf.global_variables_initializer())

    if train_dir:
        train_files = os.path.join(train_dir, '**', '*.csv')
        train_filenames = glob.glob(train_files, recursive=True)
        train_data, train_target = data.get(train_filenames, target_num, window_size)

        number_of_steps = epoch_to_steps(epoch, train_data.shape[0], batch_size)
        model.train(train_data, train_target, batch_size, number_of_steps, load)

    if test_dir:
        test_files = os.path.join(test_dir, '**', '*.csv')
        test_filenames = glob.glob(test_files, recursive=True)
        test_data, test_target = data.get(test_filenames, target_num, window_size)

        predictions, _ = model.test(test_data, test_target, load)
        model.confusion_matrix(predictions, test_target, logdir)


if __name__ == '__main__':
    fire.Fire(main)
