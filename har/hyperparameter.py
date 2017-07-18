from abc import ABC, abstractmethod
import numpy as np


class Hyperparameter(ABC):
    """Abstract class of hyperparameter

    Args:
        - `learning_rate`: learning rate hyperparameter

    """
    def __init__(self, learning_rate):
        self.learning_rate = learning_rate

def random_learning_rate(lr_min, lr_max):
    """Generates random learning rate"""
    return 10 ** np.random.uniform(lr_min, lr_max)
    