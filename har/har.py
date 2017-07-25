"""Human Activity Recognition Model Trainer

Usage example:
$ python har.py --train_dataset=/path/to/train/dataset \
                --validation_dataset=/path/to/validation/dataset \
                --test_dataset=/path/to/test/dataset \
                --epoch=300 \
                --logdir=logs

"""

import glob
import os
import fire
import numpy as np
import tensorflow as tf
import data
from convlstm import ConvLSTM, generate_hyperparameters as convlstm_hyperparamter


NUM_TARGET = 6
WINDOW_SIZE = 100

def get_filenames(basedir):
    """Get a list of every csv file in `basedir`"""
    pattern = os.path.join(basedir, '**', '*.csv')

    return glob.glob(pattern, recursive=True)

def load(path, num_target, window_size):
    """Load dataset from file or directory

    Args:
        - `path`:           path to dataset file or directory
        - `num_target`:     number of class in the dataset
        - `window_size`:    sliding window size

    Returns:
        A `Dataset` containing `data` and `target`

    """
    _, extension = os.path.splitext(path)

    if extension:
        return load_dataset(path, num_target)
    else:
        return load_dir(path, num_target, window_size)

def load_dir(basedir, num_target, window_size):
    """Load dataset from every csv file in `basedir`

    Args:
        - `basedir`:        path to dataset directory
        - `num_target`:     number of class in the dataset
        - `window_size`:    sliding window size

    Returns:
        A `Dataset` containing `data` and `target`

    """
    filenames = get_filenames(basedir)
    features, target = data.get(filenames, num_target, window_size)

    return data.Dataset(features, target)

def load_dataset(filename, num_target):
    """Load dataset from a file

    Args:
        - `filename`:       dataset file path
        - `num_target`:     number of class in the dataset
        - `window_size`:    sliding window size

    Returns:
        A `Dataset` containing `data` and `target`

    """
    dataset = np.loadtxt(filename, delimiter=',')
    features = dataset[:, :600]
    target = dataset[:, 600:601]
    target = tf.one_hot(target, num_target)
    target = tf.reshape(target, [-1, num_target])

    return data.Dataset(features, target)

def print_hyperparameter_notes(hyperparameter):
    """Print hyperparameter settings to console"""
    print('Convolutional layers: %s' % str(hyperparameter.conv_layers))
    print('LSTM layers: %s' % str(hyperparameter.lstm_layers))
    print('Learning rate: %f' % hyperparameter.learning_rate)
    print('Dropout keep probability: %f' % hyperparameter.keep_prob)

def write_hyperparameter_notes(hyperparameter, logdir):
    """Write `hyperparameter` settings in a file on `logdir` directory

    Args:
        - `hyperparameter`: hyperparameter to write
        - `logdir`:         directory to save the file

    """
    path = os.path.join(logdir, 'hyperparameter.txt')
    with open(path, 'w') as f:
        f.write('Convolutional layers: %s\n' % str(hyperparameter.conv_layers))
        f.write('LSTM layers: %s\n' % str(hyperparameter.lstm_layers))
        f.write('Learning rate: %f\n' % hyperparameter.learning_rate)
        f.write('Dropout keep probability: %f' % hyperparameter.keep_prob)

def main(train_dataset=None, validation_dataset=None, test_dataset=None, epoch=30, batch_size=128,
         logdir=None, run=1, variation=1, checkpoint=None):
    """HAR Model Trainer

    Train the `ConvLSTM` model with several `variation` of `ConvLSTMHyperparameter`. The model is
    trained with dataset from `train_dataset`, validated with dataset from `validation_dataset` and
    tested with dataset from `test_dataset`.

    Training summary and checkpoints is saved to `logdir`. A log directory is created for each
    variation, started from number provided to `run`.

    To restore a checkpoint before training or testing, provide the path to `checkpoint`.

    Args:
        - `train_dataset`:      path to train dataset
        - `validation_dataset`: path to validation dataset
        - `test_dataset`:       path to test dataset
        - `epoch`:          number of epoch to train
        - `batch_size`:     mini batch size used for training
        - `logdir`:         path to save checkpoint and summary
        - `run`:            number of run for the first variation, used for log directory naming
        - `variation`:      number of hyperparameter variation
        - `checkpoint`:     checkpoint path to restore

    """
    hyperparameters = convlstm_hyperparamter(variation)
    for i, hyperparameter in enumerate(hyperparameters):
        run_logdir = os.path.join(logdir, 'run' + str(i + run))
        model = ConvLSTM(hyperparameter, run_logdir)
        print('Run %d/%d' % (i + 1, variation))
        print_hyperparameter_notes(hyperparameter)
        write_hyperparameter_notes(hyperparameter, run_logdir)

        if train_dataset and validation_dataset:
            train_data = load(train_dataset, NUM_TARGET, WINDOW_SIZE)
            validation_data = load(validation_dataset, NUM_TARGET, WINDOW_SIZE)

            if train_data.data.any() and validation_data.data.any():
                model.train(train_data, validation_data, epoch, batch_size, checkpoint)

        if test_dataset:
            test_data = load(test_dataset, NUM_TARGET, WINDOW_SIZE)

            if test_data.data.any():
                prediction = model.test(test_data, batch_size, checkpoint)
                model.confusion_matrix(prediction, test_data.target)

        tf.reset_default_graph()

if __name__ == '__main__':
    fire.Fire(main)
