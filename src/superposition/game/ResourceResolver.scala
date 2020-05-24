package superposition.game

import java.io.InputStream
import java.net.URL

import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.files.{FileHandle, FileHandleStream}

/** Resolves file names to file handles using the application's Java resources. */
object ResourceResolver extends FileHandleResolver {
  override def resolve(fileName: String): FileHandle = new UrlHandle(getClass.getResource(fileName))

  /** A file handle for a URL.
    *
    * @param url the URL
    */
  private class UrlHandle(url: URL) extends FileHandleStream(url.toExternalForm) {
    override def read(): InputStream = url.openStream()

    override def parent(): FileHandle = {
      val parentUrl =
        if (url.toExternalForm.endsWith("/")) new URL(url, "..")
        else new URL(url, ".")
      new UrlHandle(parentUrl)
    }

    override def child(name: String): FileHandle = {
      val childUrl =
        if (url.toExternalForm.endsWith("/")) new URL(url, name)
        else new URL(url.toExternalForm + "/" + name)
      new UrlHandle(childUrl)
    }
  }

}
