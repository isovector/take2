if exists('g:take2')
  finish
endif

python << endpython
import vim

global gBuffersOpen
global gTimer

gBuffersOpen = 0
gThread = None
gTimer = None

"""
Gets the currently visible lines in the window.
"""
def get_window_range():
    cursor = vim.current.window.cursor
    localCursor = int(vim.eval("winline()"))
    topOfWindow = cursor[0] - localCursor + 1
    return (topOfWindow, topOfWindow + vim.current.window.height) 


"""
Send all data to the daemon, piping the current buffer's content into 
stdin.
"""
def send_to_daemon(filename, window_range):
    from subprocess import Popen, PIPE
    from os import getpid

    # Transform the buffer into a string. The final \n is appended because
    # it exists in the file, but not in the buffer.
    buffer = "\n".join(vim.current.buffer[:]) + "\n"

    pipe = Popen("cat - > /tmp/take2.log", shell = True, stdin = PIPE)
    pipe.communicate(input = buffer);


"""
Collect metrics and send it to the daemon.
"""
def collect_metrics():
    send_to_daemon(
        vim.current.buffer.name,
        get_window_range()
    )



# All code below this is just magic to get the above working

"""
Set a callback for the future.
"""
def scheduleNextTimer():
    from threading import Timer

    """
    Helper function to reschedule the timer
    """
    def timerThread():
        collect_metrics()
        scheduleNextTimer()

    global gTimer
    gTimer = Timer(5.0, timerThread)
    gTimer.start()


"""
Called when vim opens a new buffer. Keep track of it so we know when
all buffers are closed. If this is the first buffer being opened, start
our metric collection thread.
"""
def openBuffer():
    global gBuffersOpen, gTimer

    gBuffersOpen += 1
    if not gTimer:
        # start the initial timer
        scheduleNextTimer()


"""
Called when vim closes a new buffer. If this is the last buffer being
closed, destroy our metric collection thread so we can exit vim.
"""
def closeBuffer():
    global gBuffersOpen
    global gThread
    global gTimer

    gBuffersOpen -= 1
    if gBuffersOpen == 0:
        # interrupt our sleep
        vim.command("echo \"killing it\"")
        gTimer.cancel()
        gTimer = None
endpython

command! -bar
\ SendTake2
\ call s:cmd_Send(<q-args>, expand('<abuf>') == '')

function! s:cmd_Send(name, interactive_use_p)
    python << endpython


endpython
  if &l:buftype != ''
    if a:interactive_use_p
      echo 'This buffer is not a normal one. Skeleton leaves it as is.'
    endif
    return
  endif
endfunction

function! s:cmd_SkeletonLoad_complete(arglead, cmdline, cursorpos)
  return map(split(globpath(&runtimepath, s:SKELETON_DIR.a:arglead.'*'), "\n"),
  \ 'fnamemodify(v:val, ":t")')
endfunction


augroup plugin-skeleton
  autocmd!
  autocmd BufNewFile * call s:on_BufNewFile()
  autocmd BufWinEnter * call s:on_EnterWin()
  autocmd BufWinLeave * call s:on_CloseWin()
augroup END


function! s:on_EnterWin()
    python << endpython
openBuffer()
endpython
endfunction

function! s:on_CloseWin()
    python << endpython
closeBuffer()
endpython
endfunction

function! s:on_BufNewFile()
  silent doautocmd User plugin-skeleton-detect

  if &l:filetype != ''
    execute 'SkeletonLoad' &l:filetype
  endif
endfunction




let g:loaded_take2 = 1

" __END__
" vim: foldmethod=marker
