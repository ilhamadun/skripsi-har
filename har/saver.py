import os
import tensorflow as tf

class Saver:
    """Checkpoint and summary saver"""
    def __init__(self, session, logdir):
        self.session = session
        self._initialize_logs(os.path.join(logdir, 'summary'))
        self._initialize_checkpoint(os.path.join(logdir, 'checkpoint'))
        self._initialize_saver()

    def _initialize_logs(self, logdir):
        train_path = os.path.join(logdir, 'train')
        test_path = os.path.join(logdir, 'test')

        self.log_train = tf.summary.FileWriter(train_path, graph=tf.get_default_graph())
        self.log_test = tf.summary.FileWriter(test_path, graph=tf.get_default_graph())

    def _initialize_checkpoint(self, savedir):
        self.checkpoint_directory = {
            'best': os.path.join(savedir, 'best'),
            'last': os.path.join(savedir, 'last')
        }
        self.checkpoint_path = {'best': None, 'last': None}

        for _, directory in self.checkpoint_directory.items():
            os.makedirs(directory)

    def _initialize_saver(self):
        self.saver = {
            'best': tf.train.Saver(max_to_keep=1),
            'last': tf.train.Saver(max_to_keep=10)
        }

    def save_graph_def(self, filename):
        """Write default graph definition to a file

        Args:
            - `filename`: Path to save the proto file

        """
        with self.session.as_default():
            saved_to = tf.train.write_graph(self.session.graph.as_graph_def(),
                                            self.checkpoint_directory['best'],
                                            filename)

            print('Graph definition saved to', saved_to)

    def save_checkpoint(self, save_type, step):
        """Save variables to a checkpoint file

        Args:
            - `save_type`:  either 'best' or 'last'
            - `step`:       number appended to the checkpoint file name

        """
        assert save_type in ['best', 'last']

        saver = self.saver[save_type]

        checkpoint_prefix = os.path.join(self.checkpoint_directory[save_type], 'saved_checkpoint')
        self.checkpoint_path[save_type] = saver.save(self.session, checkpoint_prefix,
                                                     global_step=step)

    def save_summary(self, save_type, summary, step):
        """Save summary

        Args:
            - `save_type`:  summary type, `train` or `test`
            - `summary`:    summary to save
            - `step`:       train or test step of summary

        """
        assert save_type in ['train', 'test']

        if save_type == 'train':
            self.log_train.add_summary(summary, step)
        else:
            self.log_test.add_summary(summary, step)

    def restore_checkpoint(self, path):
        """Restore graph variables from a checkpoint

        Args:
            - `path`: path to checkpoint

        """
        try:
            self.saver['last'].restore(self.session, path)
            print('Checkpoint restored from %s' % path)
        except ValueError:
            print('Load path is invalid')
