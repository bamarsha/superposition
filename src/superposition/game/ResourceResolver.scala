package superposition.game

import java.io.File

import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.files.FileHandle
import scalaz.Scalaz._

/** Resolves file names to file handles using the application's Java resources. */
object ResourceResolver extends FileHandleResolver {
  override def resolve(fileName: String): FileHandle =
    (fileName
      |> getClass.getResource
      |> (_.toURI)
      |> (new File(_))
      |> (new FileHandle(_)))
}
