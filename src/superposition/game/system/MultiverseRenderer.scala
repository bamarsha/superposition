package superposition.game.system

import com.badlogic.ashley.core._
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import superposition.game.component.{Beam, Multiverse, Position, SpriteView}

import scala.jdk.CollectionConverters._

final class MultiverseRenderer extends EntitySystem {
  private var multiverses: Iterable[Entity] = Nil

  private val spriteBatch: SpriteBatch = new SpriteBatch

  private val shapeRenderer = new ShapeRenderer

  override def addedToEngine(engine: Engine): Unit =
    multiverses = engine.getEntitiesFor(Family.all(classOf[Multiverse]).get).asScala

  override def update(deltaTime: Float): Unit =
    for (multiverse <- multiverses map Multiverse.Mapper.get) {
      highlightOccupiedCells(multiverse)
      drawSprites(multiverse)
      drawBeams(multiverse)
    }

  private def highlightOccupiedCells(multiverse: Multiverse): Unit = {
    val occupiedCells =
      (for {
        entity <- multiverse.entities if Position.Mapper.has(entity)
        position = Position.Mapper.get(entity)
        universe <- multiverse.universes
      } yield universe.state(position.cell)).toSet

    gl.glEnable(GL_BLEND)
    shapeRenderer.setProjectionMatrix(multiverse.camera.combined)
    shapeRenderer.begin(ShapeType.Filled)
    shapeRenderer.setColor(1, 1, 1, 0.3f)
    for (cell <- occupiedCells) {
      shapeRenderer.rect(cell.x, cell.y, 1, 1)
    }
    shapeRenderer.end()
    gl.glDisable(GL_BLEND)
  }

  private def drawSprites(multiverse: Multiverse): Unit = {
    val spriteViews = (multiverse.entities filter SpriteView.Mapper.has map SpriteView.Mapper.get)
      .toSeq sortBy (_.layer)
    spriteBatch.setProjectionMatrix(multiverse.camera.combined)
    spriteBatch.begin()
    for (spriteView <- spriteViews; universe <- multiverse.universes) {
      spriteView.draw(spriteBatch, universe)
    }
    spriteBatch.end()
  }

  private def drawBeams(multiverse: Multiverse): Unit =
    for (beam <- multiverse.entities filter Beam.Mapper.has map Beam.Mapper.get;
         universe <- multiverse.universes) {
      beam.draw(universe)
    }
}
