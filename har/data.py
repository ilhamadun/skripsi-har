"""Dataset loading and proprocessing"""

import tensorflow as tf
import numpy as np


class Dataset:
    """Structure of dataset. Containing `data` and `target`"""
    def __init__(self, data, target):
        self.data = data
        self.target = target


def get(filenames, num_target, window_size, overlap=0.5, divider=None):
    """Get data and prepares it for training and testing

    This method do all the preparation needed for training and testing process,
    including loading, shuffling and dividing data to train and test group

    Args:
        - `filenames`:      list of filename to open
        - `num_target`:     number of target class
        - `window_size`:    size of window to group the data
        - `overlap`:        overlap size of the sliding window
        - `divider`:        ratio for dividing train and test data

    """
    data, target = load(filenames, window_size, overlap)
    data, target = shuffle(data, target)

    if divider:
        data_train, data_test = divide(data, divider)
        target_train, target_test = divide(target, divider)

        target_train = tf.one_hot(target_train, num_target)
        target_test = tf.one_hot(target_test, num_target)

        return data_train, data_test, target_train, target_test
    else:
        target = tf.one_hot(target, num_target)

        return data, target


def load(filenames, window_size, overlap):
    """Load csv files

    Args:
        - `filenames`:      list of filename to open
        - `window_size`:    size of window to group the data
        - `overlap`:        overlap size of the sliding window
    """
    data = None
    target = None

    for _, filename in enumerate(filenames):
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


def sliding_window(data_in, window_size, overlap=0.5):
    """Apply sliding windows to data

    Args:
        - `data_in`:        data array
        - `window_size`:    size of window to group the data

    """
    step = int(window_size * (1 - overlap))
    data, target = [], []

    for i in range(0, data_in.data.shape[0] - window_size, step):
        if data_in.target[i] != data_in.target[i + window_size]:
            continue

        data.append(np.array(data_in.data[i:i + window_size].flatten()))
        target.append(data_in.target[i])

    return np.vstack(data), np.array(target)


def shuffle(data, target):
    """Shuffle the data

    Args:
        - `data`:    data to shuffle
        - `target`:  target to shuffle

    """
    target = np.reshape(target, [target.shape[0], 1])
    sample = np.concatenate((data, target), axis=1)
    np.random.shuffle(sample)

    data, target = np.hsplit(sample, [data.shape[1]])
    target = np.reshape(target, [target.shape[0], ])

    return data, target


def divide(arr, divider):
    """Divide data to train annd test group

    Args:
        - `arr`:     array of data to divide
        - `divider`: [divider] ratio goes to train data, the rest goes to test data
    """
    train = arr[0:int(arr.shape[0] * divider)]
    test = arr[int(arr.shape[0] * divider):]

    return train, test


def iterator(data, target, length, batch_size):
    """Creates mini batch over data and target

    Args:
        - `data`:
    """
    while True:
        for batch_idx in range(0, length, batch_size):
            batch_data = data[batch_idx:batch_idx + batch_size]
            batch_label = target[batch_idx:batch_idx + batch_size]
            yield batch_data, batch_label
