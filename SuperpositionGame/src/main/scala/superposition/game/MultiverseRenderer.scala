package superposition.game

import com.badlogic.ashley.core._
import com.badlogic.gdx.Gdx.gl
import com.badlogic.gdx.graphics.GL20.GL_BLEND
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import superposition.game.MultiverseRenderer.{MultiverseMapper, PositionMapper, SpriteViewMapper}

import scala.jdk.CollectionConverters._

private final class MultiverseRenderer extends EntitySystem {
  private var multiverses: Iterable[Entity] = Nil

  private val spriteBatch: SpriteBatch = new SpriteBatch

  private val shapeRenderer = new ShapeRenderer

  override def addedToEngine(engine: Engine): Unit = {
    multiverses = engine.getEntitiesFor(Family.all(classOf[Multiverse]).get).asScala
  }

  override def update(deltaTime: Float): Unit =
    for (multiverse <- multiverses map MultiverseMapper.get) {
      highlightOccupiedCells(multiverse)
      drawSprites(multiverse)
    }

  private def highlightOccupiedCells(multiverse: Multiverse): Unit = {
    val occupiedCells =
      (for {
        entity <- multiverse.entities if PositionMapper.has(entity)
        position = PositionMapper.get(entity)
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
    spriteBatch.setProjectionMatrix(multiverse.camera.combined)
    spriteBatch.begin()
    for (entity <- multiverse.entities if SpriteViewMapper.has(entity);
         spriteView = SpriteViewMapper.get(entity);
         universe <- multiverse.universes) {
      spriteView.draw(spriteBatch, universe)
    }
    spriteBatch.end()
  }
}

private object MultiverseRenderer {
  private val MultiverseMapper: ComponentMapper[Multiverse] = ComponentMapper.getFor(classOf[Multiverse])

  private val SpriteViewMapper: ComponentMapper[SpriteView] = ComponentMapper.getFor(classOf[SpriteView])

  private val PositionMapper = ComponentMapper.getFor(classOf[Position])
}
