if exists('g:take2')
  finish
endif

python << endpython
import vim
from os import unlink, write, close
from shutil import rmtree
from tempfile import mkstemp, mkdtemp

global gTimer, gIsActive, gUpdatesToSend, gIdleCount, IS_IDLE_COUNT, RECORD_DURATION

gTimer = None
gIsActive = False
gUpdatesToSend = 0
gIdleCount = 0

RECORD_DURATION = 5.0
IS_IDLE_COUNT = 12

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


def get_window_range():
    """
    Gets the currently visible lines in the window.
    """
    cursor = vim.current.window.cursor
    localCursor = int(vim.eval("winline()"))
    topOfWindow = cursor[0] - localCursor + 1
    return (topOfWindow, topOfWindow + vim.current.window.height)


def send_to_daemon(filename, buffer, window_range):
    """
    Send all data to the daemon, piping the current buffer's content into
    stdin.
    """
    from subprocess import Popen, PIPE
    from os import getpid

    tmp = make_tempfile(buffer)

    cmd = "accio send --start=%d --end=%d --filename=%s --buffer=%s &" % (
        window_range[0],
        window_range[1],
        filename,
        tmp
    )

    pipe = Popen(
        cmd,
        shell = True,
        stdin = PIPE,
        stdout = PIPE,
        stderr = PIPE
    )

    pipe.communicate(input = buffer)
    delete_tempfile(tmp)


def collect_metrics():
    """
    Collect metrics and send it to the daemon.
    """
    from threading  import Thread

    global gUpdatesToSend
    if gUpdatesToSend > 0:
        t = Thread(
            target = send_to_daemon,
            args = (
                vim.current.buffer.name,
                "\n".join(vim.current.buffer[:]) + "\n",
                get_window_range()
            )
        )

        t.daemon = True
        t.start()

        gUpdatesToSend -= 1



# All code below this is just magic to get the above working

def scheduleNextTimer():
    """
    Set a callback for the future.
    """
    from threading import Timer
    global RECORD_DURATION

    def timerThread():
        """
        Helper function to reschedule the timer
        """
        global gIdleCount, gUpdatesToSend
        if isActive():
            gUpdatesToSend += 1
        gIdleCount += 1
        scheduleNextTimer()

    global gTimer
    gTimer = Timer(RECORD_DURATION, timerThread)
    gTimer.start()

def isActive():
    """
    Check whether or not there has been input lately.
    """
    global gIdleCount, IS_IDLE_COUNT
    return gIdleCount < IS_IDLE_COUNT

def setActive():
    """
    Called by vim to inform us the user has moved the cursor.
    """
    global gIdleCount
    gIdleCount = 0
    collect_metrics()

def setIdle():
    """
    Called by vim to inform us we lost focus.
    """
    global gIdleCount, gUpdatesToSend, IS_IDLE_COUNT
    gIdleCount = IS_IDLE_COUNT
    gUpdatesToSend = 0

endpython

augroup plugin-take2
  autocmd!
  autocmd CursorMoved *  call s:on_Activity()
  autocmd CursorMovedI * call s:on_Activity()
  autocmd FocusGained * call s:on_Activity()

  autocmd FocusLost * call s:on_Idle()
  autocmd VimEnter * call s:on_Enter()
  autocmd VimLeave * call s:on_Exit()
augroup END


function! s:on_Activity()
    python << endpython
setActive()
endpython
endfunction

function! s:on_Idle()
    python << endpython
setIdle()
endpython
endfunction

function! s:on_Enter()
    python << endpython
scheduleNextTimer()
endpython
endfunction

function! s:on_Exit()
    python << endpython
gTimer.cancel()
gTimer = None
endpython
endfunction

let g:loaded_take2 = 1

" __END__
" vim: foldmethod=marker
