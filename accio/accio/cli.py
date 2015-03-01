from accio.parser import build_parser
from accio.commands.translate import translate
from accio.commands.send import send
from accio.commands.pull import getCoeffsForFile
from sys import stderr, stdout, stdin, argv



import xmlrpclib
from SimpleXMLRPCServer import SimpleXMLRPCServer

OPTIONS = {
    'command': str,
    '--old_commit': str,
    '--new_commit': str,
    '--filename': str,
    '--repo_path': str,
}

def main():
    # We want our accio to be able to just use the translate function. 
    # This is used on the backend server to translate diffs between commits
    if len(argv) > 1:
        parser = build_parser(OPTIONS)
        (opt, args) = parser.parse_known_args()
        if opt.command != "translate":
            print "Invalid command, either run the server with just 'accio' or provide the correct parameters for 'translate'"
            return 1
        # Pass our paremeters down and read the map of line numbers -> number of times looked at from stdin
        print translate(opt.old_commit, opt.new_commit, opt.filename, opt.repo_path, stdin.read())
    else:
        # Otherwise we are running the accio on the user's machine so set it up to send the info to our server
        server = SimpleXMLRPCServer(("localhost", 7432))
        server.register_function(send, "snapshot")
        server.register_function(translate, "translate")
        server.register_function(getCoeffsForFile, "coefficients")

        server.serve_forever()

    return 0
