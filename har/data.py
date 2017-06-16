import tensorflow as tf
import numpy as np


def get(filenames, num_target, window_size, overlap=0.5, divider=None, one_hot=True):
    data, target = load(filenames, window_size, overlap)
    data, target = shuffle(data, target)

    if divider:
        data_train, data_test = divide(data, divider)
        target_train, target_test = divide(target, divider)

        if one_hot:
            target_train = tf.one_hot(target_train, num_target)
            target_test = tf.one_hot(target_test, num_target)

        return data_train, data_test, target_train, target_test
    else:
        if one_hot:
            target = tf.one_hot(target, num_target)

        return data, target


def load(filenames, window_size, overlap):
    data = None
    target = None

    for i, filename in enumerate(filenames):
        file = tf.contrib.learn.datasets.base.load_csv_with_header(
            filename=filename,
            features_dtype=np.float32,
            target_dtype=np.int,
            target_column=-1)

        file_data, file_target = sliding_window(file, window_size, overlap)
        if data is None or target is None:
            data = np.array(file_data)
            target = np.array(file_target)
        else:
            data = np.concatenate((data, file_data))
            target = np.concatenate((target, file_target))

    return data, target


def sliding_window(data_in, window_size, overlap = 0.5):
    step = int(window_size * (1 - overlap))
    data, target = [], []

    for i in range(0, data_in.data.shape[0] - window_size, step):
        if data_in.target[i] != data_in.target[i + window_size]:
            continue

        data.append(np.array(data_in.data[i:i + window_size].flatten()))
        target.append(data_in.target[i])

    return np.vstack(data), np.array(target)


def shuffle(data, target):
    target = np.reshape(target, [target.shape[0], 1])
    sample = np.concatenate((data, target), axis=1)
    np.random.shuffle(sample)

    data, target = np.hsplit(sample, [data.shape[1]])
    target = np.reshape(target, [target.shape[0], ])

    return data, target


def divide(arr, divider):
    train = arr[0:int(arr.shape[0] * divider)]
    test = arr[int(arr.shape[0] * divider):]

    return train, test


def iterator(data, target, length, batch_size):
    while True:
        for batch_idx in range(0, length, batch_size):
            batch_data = data[batch_idx:batch_idx + batch_size]
            batch_label = target[batch_idx:batch_idx + batch_size]
            yield batch_data, batch_label


def load_model(sess, path):
    """ Load model from path

    Keyword arguments:
    sess -- tensorflow session
    path -- path to load the model
    """
    saver = tf.train.Saver()
    try:
        saver.restore(sess, path)
        print("Model restored from %s" % path)
    except ValueError:
        print("Load path is invalid.")


def save_model(sess, path):
    """ Save model to path

    Keyword arguments:
    sess -- tensorflow session
    path -- path to save the model
    """
    saver = tf.train.Saver()
    try:
        save_path = saver.save(sess, path)
        print("Model saved in %s" % save_path)
    except tf.python.framework.errors.FailedPreconditionError:
        print("Save path is invalid.")
