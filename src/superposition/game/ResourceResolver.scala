package superposition.game

import java.io.File

import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.files.FileHandle

private object ResourceResolver extends FileHandleResolver {
  override def resolve(fileName: String): FileHandle =
    new FileHandle(new File(getClass.getResource(fileName).toURI))
}
