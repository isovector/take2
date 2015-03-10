import sublime, sublime_plugin
import xmlrpclib, socket
from os import unlink, write, close, getpid
from tempfile import mkstemp
from subprocess import Popen, PIPE
from threading  import Thread, Timer

def make_tempfile(content=None):
    """ Creates a temporary file and returns the path. """
    fd, tmpfile = mkstemp()

    if content:
        write(fd, content)

    close(fd)
    return tmpfile

def delete_tempfile(path):
    """ Deletes a temporary file """
    try:
        unlink(path)
    except:
        pass

class ExampleCommand(sublime_plugin.EventListener):
    RECORD_DURATION = 5.0
    IS_IDLE_COUNT = 12

    def __init__(self):
        self.timer = None
        self.updatesToSend = 0
        self.idleCount = 0
        self.accio = None

    def send_to_daemon(self, filename, buffer, window_range):
        """
        Send all data to the daemon, piping the current buffer's content into
        stdin.
        """
        tmp = make_tempfile(buffer)
        try:
            self.accio.snapshot(
                filename,
                window_range[0],
                window_range[1],
                tmp)
        except:
            pass
        delete_tempfile(tmp)

    def get_window_range(self, view):
        """
        Gets the currently visible lines in the window.
        """ 
        region = view.visible_region()

        start = view.rowcol(region.a)[0] + 1
        end = view.rowcol(region.b)[0] + 1

        return (start, end)


    def collect_metrics(self, view):
        """
        Collect metrics and send it to the daemon.
        """
        if self.updatesToSend > 0:
            t = Thread(
                target = self.send_to_daemon,
                args = (
                    view.file_name(),
                    view.substr(sublime.Region(0, view.size())),
                    self.get_window_range(view)
                )
            )

            t.daemon = True
            t.start()

            self.updatesToSend -= 1

    def timerThread(self):
        """
        Helper function to reschedule the timer
        """
        if self.isActive():
            self.updatesToSend += 1
        self.idleCount += 1
        self.scheduleNextTimer()

    def scheduleNextTimer(self):
        """
        Set a callback for the future.
        """
        self.timer = Timer(self.RECORD_DURATION, self.timerThread)
        self.timer.start()

    def isActive(self):
        """
        Check whether or not there has been input lately.
        """
        return self.idleCount < self.IS_IDLE_COUNT

    def setActive(self, view):
        """
        Called by vim to inform us the user has moved the cursor.
        """
        self.idleCount = 0
        self.collect_metrics(view)

    def setIdle(self):
        """
        Called by vim to inform us we lost focus.
        """
        self.idleCount = self.IS_IDLE_COUNT
        self.updatesToSend = 0

    def on_selection_modified(self, view):
        self.setActive(view)

    def on_deactivated(self, view):
        self.setIdle();

    def on_new(self, view):
        Popen(['accio'], stdout=PIPE, stderr=PIPE, shell=True)

        self.accio = xmlrpclib.ServerProxy("http://localhost:7432/")
        socket.setdefaulttimeout(int(self.RECORD_DURATION) - 1)
        
        self.scheduleNextTimer()