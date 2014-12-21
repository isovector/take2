package utils

import java.net.URL
import org.apache.xmlrpc.client.XmlRpcClient
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory
import scala.collection.JavaConverters._

class RpcClient(serverURL: String) {
    val config = new XmlRpcClientConfigImpl();
    config.setServerURL(new URL(serverURL));
    config.setEncoding("ISO-8859-1");
    val client = new XmlRpcClient();
    client.setTransportFactory(new XmlRpcSunHttpTransportFactory(client));
    client.setConfig(config);

    def translate(
        oldCommit: String,
        newCommit: String,
        filePath: String,
        repoPath: String,
        input: String) = {
      client.execute("translate",
        List(oldCommit, newCommit, filePath, repoPath, input).asJava)
    }
}

