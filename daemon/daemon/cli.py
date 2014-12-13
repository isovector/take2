from daemon.parser import build_parser
from daemon.commands.translate import translate
from daemon.commands.send import send
from daemon.commands.pull import getCoeffsForFile

import xmlrpclib
from SimpleXMLRPCServer import SimpleXMLRPCServer

def main():
    server = SimpleXMLRPCServer(("localhost", 7432))
    server.register_function(send, "snapshot")
    server.register_function(translate, "translate")
    server.register_function(getCoeffsForFile, "coefficients")

    server.serve_forever()
    return 0
