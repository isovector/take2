from requests import post, get
from urlparse import urljoin


class Connection(object):
    """ Abstracted requests object to make it easy to test """

    def __init__(self, url):
        self.__url = url

    def post(self, payload, path):
        """ Sends a payload to the connection's URL with a payload """
        return post(urljoin(self.__url, path), data=payload)

    def get(self, path):
        return get(urljoin(self.__url, path))
