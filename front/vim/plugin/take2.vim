if exists('g:take2')
  finish
endif

python << endpython
import vim

global gTimer, gIsActive, gIdleCount, IS_IDLE_COUNT, RECORD_DURATION

gTimer = None
gIsActive = False
gIdleCount = 0

RECORD_DURATION = 5.0
IS_IDLE_COUNT = 12

def get_window_range():
    """
    Gets the currently visible lines in the window.
    """
    cursor = vim.current.window.cursor
    localCursor = int(vim.eval("winline()"))
    topOfWindow = cursor[0] - localCursor + 1
    return (topOfWindow, topOfWindow + vim.current.window.height) 


def send_to_daemon(filename, window_range):
    """
    Send all data to the daemon, piping the current buffer's content into 
    stdin.
    """
    from subprocess import Popen, PIPE
    from os import getpid

    # Transform the buffer into a string. The final \n is appended because
    # it exists in the file, but not in the buffer.
    buffer = "\n".join(vim.current.buffer[:]) + "\n"
    cmd = "accio --start=%d --end=%d --filename=%s &" % (
        window_range[0],
        window_range[1],
        filename
    )

    pipe = Popen(
        cmd,
        shell = True, 
        stdin = PIPE,
        stdout = PIPE,
        stderr = PIPE
    )
    pipe.communicate(input = buffer);


def collect_metrics():
    """
    Collect metrics and send it to the daemon.
    """
    send_to_daemon(
        vim.current.buffer.name,
        get_window_range()
    )



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
        global gIdleCount
        if isActive():
            collect_metrics()
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

def setIdle():
    """
    Called by vim to inform us we lost focus.
    """
    global gIdleCount, IS_IDLE_COUNT
    gIdleCount = IS_IDLE_COUNT

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
