import tensorflow as tf
import numpy as np


def get(filenames, num_target, window_size, overlap=0.5, divider=0.7):
    """ Get data and prepares it for training and testing

    This method do all the preparation needed for training and testing process,
    including loading, shuffling and dividing data to train and test group

    filenames   -- list of filename to open
    num_target  -- number of target class
    window_size -- size of window to group the data
    overlap     -- overlap size of the sliding window
    divider     -- ratio for dividing train and test data
    """
    data, target = load(filenames, window_size, overlap)
    data, target = shuffle(data, target)

    data_train, data_test = divide(data, divider)
    target_train, target_test = divide(target, divider)

    target_train = tf.one_hot(target_train, num_target)
    target_test = tf.one_hot(target_test, num_target)

    return data_train, data_test, target_train, target_test


def load(filenames, window_size, overlap, one_hot=False, target_num=10):
    """ Load csv files

    filenames   -- list of filename to open
    window_size -- size of window to group the data
    overlap     -- overlap size of the sliding window
    """
    data = None
    target = None

    for i, filename in enumerate(filenames):
        file = tf.contrib.learn.datasets.base.load_csv_with_header(
            filename=filename,
            features_dtype=np.float32,
            target_dtype=np.int,
            target_column=-1)

        file_data, file_target = sliding_window(file, window_size)
        if data is None or target is None:
            data = np.array(file_data)
            target = np.array(file_target)
        else:
            data = np.concatenate((data, file_data))
            target = np.concatenate((target, file_target))

    if one_hot:
        target = tf.one_hot(target, target_num)

    return data, target


def sliding_window(data_in, window_size):
    """ Apply sliding windows to data

    data_in     -- data array
    window_size -- size of window to group the data
    """
    overlap = 0.5
    step = int(window_size * (1 - overlap))
    data = np.empty([int(data_in.data.shape[0] / (window_size * overlap)),
                     data_in.data.shape[1] * window_size])
    target = np.empty([int(data_in.target.shape[0] / (window_size * overlap))])

    for i in range(0, data_in.data.shape[0] - window_size, step):
        if data_in.target[i] != data_in.target[i + window_size]:
            continue

        data[int(i / step)] = data_in.data[i:i + window_size].flatten()
        target[int(i / step)] = data_in.target[i]

    return data, target


def shuffle(data, target):
    """ Shuffle the data

    data   -- data to shuffle
    target -- target to shuffle
    """
    target = np.reshape(target, [target.shape[0], 1])
    sample = np.concatenate((data, target), axis=1)
    np.random.shuffle(sample)

    data, target = np.hsplit(sample, [data.shape[1]])
    target = np.reshape(target, [target.shape[0], ])

    return data, target


def divide(arr, divider):
    """ Divide data to train annd test group

    arr      -- array of data to divide
    divider  -- [divider] ratio goes to train data, the rest goes to test data
    """
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
