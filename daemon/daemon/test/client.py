import xmlrpclib
accio = xmlrpclib.ServerProxy("http://localhost:7432/")

print repr(accio.coefficients("/home/bootstrap/Projects/take2/front/vim/plugin/take2.vim"))
