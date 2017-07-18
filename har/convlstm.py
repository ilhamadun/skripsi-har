"""ConvLSTM model, ConvLSTMHyperparameter and random hyperparameter generator"""

import numpy as np
import tensorflow as tf
import tensorflow.contrib.slim as slim
from base import BaseModel
from hyperparameter import Hyperparameter, random_learning_rate
from saver import Saver


class ConvLSTM(BaseModel):
    """ConvLSTM model

    The ConvLSTM model is built with stack of convolution and LSTM layers. The model configuration
    is set with constructor's `hyperparameter` argument.

    Args:
        - `hyperparameter`: instance of `ConvLSTMHyperparameter` as the model's configuration
        - `logdir`:         directory path to save summary and checkpoint

    """
    def __init__(self, hyperparamter, logdir):
        super(ConvLSTM, self).__init__()

        self.hyperparameter = hyperparamter

        self.build_graph()

        self.session = tf.InteractiveSession(graph=self.target.graph)
        self.saver = Saver(self.session, logdir)

        self.session.run(tf.global_variables_initializer())
        self.session.run(tf.local_variables_initializer())

    def build_graph(self):
        """Construct the graph of neural network

        As of 18-07-2017, the architecture is constructed with these configuration:
            1. Convolution layers
            2. LSTM layers
            3. Dropout
            4. Softmax
            5. Cross entropy loss function
            6. RMSProp optimizer

        Each layer's hyperparameter is defined in `self.hyperparameter`.

        """
        features = self.magnitude()
        conv_input = tf.reshape(features, [-1, 100, 8])
        conv = slim.stack(conv_input, slim.convolution, self.hyperparameter.conv_layers,
                          scope='conv')
        lstm = self._lstm_stack(conv)
        lstm_dropout = slim.dropout(lstm, self.hyperparameter.keep_prob,
                                    is_training=self.is_training, scope='lstm_dropout')
        self.output = slim.fully_connected(lstm_dropout, 6, activation_fn=tf.nn.softmax,
                                           scope='output')
        self.train_op = self.create_train_op(self.hyperparameter.learning_rate)
        self.accuracy_op = self.create_accuracy_op()

    def _lstm_stack(self, tensor_in):
        with tf.name_scope('LSTM'):
            lstm_input = tf.transpose(tensor_in, [1, 0, 2])
            lstm = [tf.contrib.rnn.BasicLSTMCell(num_units) for num_units in
                    self.hyperparameter.lstm_layers]
            lstm_cells = tf.contrib.rnn.MultiRNNCell(lstm)
            lstm_outputs, _ = tf.nn.dynamic_rnn(lstm_cells, lstm_input, dtype=tf.float32,
                                                time_major=True)

            return lstm_outputs[-1]


class ConvLSTMHyperparameter(Hyperparameter):
    """Hyperparameter for `ConvLSTM` model

    Args:
        - `conv_layers`:    configuration for convolutional layers
        - `lstm_layers`:    configuration for LSTM layers
        - `keep_prob`:      keep probability for dropout
        - `learning_rate`:  learning rate for optimizer

    Example:
        - `conv_layers = [(128, 5), (128, 5)]` creates two convolution layers, each with 128 output
          and kernel size of 5
        - `lstm_layers = [128, 128]` creates two LSTM layers, each with 128 hidden unit

    """
    def __init__(self, conv_layers, lstm_layers, keep_prob, learning_rate):
        super(ConvLSTMHyperparameter, self).__init__(learning_rate)
        self.conv_layers = conv_layers
        self.lstm_layers = lstm_layers
        self.keep_prob = keep_prob


def generate_hyperparameters(variations, conv_layers=None, lstm_layers=None, keep_prob=None,
                             learning_rate=None):
    """Generate a list of random `ConvLSTMHyperparameter` with length of `variations`

    If `conv_layers`, `lstm_layers`, `keep_prob` or `learning_rate` is `None`, a random value will
    be provided for each parameter.

    Args:
        - `variations`: number of variation to generate
        - `conv_layers`: convolutional layers hyperparameter
        - `lstm_layers`: LSTM layers hyperparameter
        - `keep_prob`: dropout keep probability
        - `learning_rate`: optimizer learning rate

    Returns:
        list of `ConvLSTMHyperparameter` with random value

    """
    hyperparameters = []

    for _ in range(variations):
        hyperparameter = ConvLSTMHyperparameter(
            conv_layers if conv_layers else random_conv_layers(),
            lstm_layers if lstm_layers else random_lstm_layers(),
            keep_prob if keep_prob else random_keep_prob(),
            learning_rate if learning_rate else random_learning_rate(-4, -3)
        )
        hyperparameters.append(hyperparameter)

    return hyperparameters

def random_conv_layers():
    """Generates random convolutional layer hyperparameter

    Returns:
        list of hyperparameter for each convolutional layer

    """
    number_of_layers = np.random.randint(2, 5)
    num_outputs = np.random.choice([32, 64, 128])

    layers = []
    for _ in range(number_of_layers):
        layers.append((num_outputs, 5))

    return layers

def random_lstm_layers():
    """Generates random LSTM layer hyperparameter

    Returns:
        list of hyperparameter for each LSTM layer

    """
    number_of_layers = np.random.randint(2, 5)
    num_units = np.random.choice([32, 64, 128])

    layers = []
    for _ in range(number_of_layers):
        layers.append(num_units)

    return layers

def random_keep_prob():
    """Generates random dropout keep probability"""
    return np.random.uniform(0.2, 0.8)
