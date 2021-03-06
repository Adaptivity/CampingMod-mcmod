package com.rikmuld.camping.client.gui

import java.awt.Color
import scala.collection.mutable.HashMap
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import com.rikmuld.camping.client.gui.button.ButtonItem
import com.rikmuld.camping.common.inventory.gui.ContainerCampinv
import com.rikmuld.camping.common.network.OpenGui
import com.rikmuld.camping.core.Objs
import com.rikmuld.camping.core.TextureInfo
import net.minecraft.block.Block
import net.minecraft.block.material.MapColor
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.client.renderer.entity.RenderItem
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraft.client.gui.GuiWinGame
import com.rikmuld.camping.client.gui.button.ButtonWithTab
import com.rikmuld.camping.client.gui.button.ButtonTabbed
import com.rikmuld.camping.client.gui.button.ButtonCampinv
import scala.collection.JavaConversions._
import com.rikmuld.camping.client.gui.button.ButtonCampinv
import net.minecraft.client.renderer.InventoryEffectRenderer

class GuiCampinginv(var player: EntityPlayer) extends GuiTabbed(player, new ContainerCampinv(player), new ResourceLocation(TextureInfo.GUI_CAMPINV)) with GuiWithEffect {
  this.xSize = 220
  this.ySize = 166
  
  var hasBackpack = false
  var hasKnife = false
  var lastMouseX = 0
  var lastMouseY = 0
           
  override def initGui {
    super.initGui
    init(guiLeft, guiTop, fontRendererObj)
  }
  override def initTabbed {
    addTopTab("Armor", xSize, ySize, guiLeft+24, guiTop, tabsTop.size, new ItemStack(Items.skull, 1, 3))
    addTopTab("Backpack", xSize, ySize, guiLeft+24, guiTop, tabsTop.size, new ItemStack(Objs.backpack))
    addTopTab("Crafting", xSize, ySize, guiLeft+24, guiTop, tabsTop.size, new ItemStack(Objs.knife))
    addTopTab("Configuration", xSize, ySize, guiLeft+24, guiTop, tabsTop.size, 220, 0, 16, 16)
    buttonList.asInstanceOf[java.util.List[GuiButton]].add(new ButtonCampinv(this, 2, guiLeft+(xSize-40)/2-45, guiTop+10, 40, 20, 0))
    buttonList.asInstanceOf[java.util.List[GuiButton]].add(new ButtonCampinv(this, 1, guiLeft+(xSize-40)/2, guiTop+10, 40, 20, 2))
    buttonList.asInstanceOf[java.util.List[GuiButton]].add(new ButtonCampinv(this, 0, guiLeft+(xSize-40)/2+45, guiTop+10, 40, 20, 1))
    buttonList.asInstanceOf[java.util.List[GuiButton]].add(new ButtonTabbed(3, guiLeft+(xSize)/2-64, guiTop+35, 128, 12, "Middle", 3, 0){
      enabled = Objs.config.prmInv==1&&Objs.config.secInv!=id-3})
    buttonList.asInstanceOf[java.util.List[GuiButton]].add(new ButtonTabbed(4, guiLeft+(xSize)/2-64, guiTop+47, 66, 15, "L-Top", 3, 0){
      enabled = Objs.config.prmInv==1&&Objs.config.secInv!=id-3})
    buttonList.asInstanceOf[java.util.List[GuiButton]].add(new ButtonTabbed(5, guiLeft+(xSize)/2, guiTop+47, 65, 15, "R-Top", 3, 0){
      enabled = Objs.config.prmInv==1&&Objs.config.secInv!=id-3})
    buttonList.asInstanceOf[java.util.List[GuiButton]].add(new ButtonTabbed(6, guiLeft+(xSize)/2-64, guiTop+62, 66, 15, "L-Bottom", 3, 0){
      enabled = Objs.config.prmInv==1&&Objs.config.secInv!=id-3})
    buttonList.asInstanceOf[java.util.List[GuiButton]].add(new ButtonTabbed(7, guiLeft+(xSize)/2, guiTop+62, 65, 15, "R-Bottom", 3, 0){
      enabled = Objs.config.prmInv==1&&Objs.config.secInv!=id-3})
  }
  def setContents(){
    hasKnife = (this.inventorySlots.asInstanceOf[ContainerCampinv].campinv.getStackInSlot(1) != null)
    hasBackpack = (this.inventorySlots.asInstanceOf[ContainerCampinv].campinv.getStackInSlot(0) != null)
  }
  def drawHoverdButton(id:Int, mouseX:Int, mouseY:Int){
    val list = new java.util.ArrayList[String]()
    if(id==2){
      list.add("Don't Edit MC Inventory")
      drawHoveringText(list, mouseX, mouseY, fontRendererObj)
    } else if(id==0){
      list.add("Replace MC Inventory")
      drawHoveringText(list, mouseX, mouseY, fontRendererObj)
    } else if(id==1){
      list.add("Use Button in MC Inventory")
      drawHoveringText(list, mouseX, mouseY, fontRendererObj)
    }
  }
  override def actionPerformed(button:GuiButton){
    if(button.id<3){
      Objs.config.setInv(button.id, Objs.config.secInv)
      buttonList.asInstanceOf[java.util.List[GuiButton]].foreach { 
        but2 => but2.enabled = if(but2.id<3) but2.id!=Objs.config.prmInv else Objs.config.prmInv==1&&Objs.config.secInv!=but2.id-3 
      }
    } else {
      Objs.config.setInv(Objs.config.prmInv, button.id-3)
      buttonList.asInstanceOf[java.util.List[GuiButton]].filter(_.id>=3).foreach { 
        but2 => but2.enabled = Objs.config.prmInv==1&&Objs.config.secInv!=but2.id-3 
      }
    }
  }
  protected override def drawGuiContainerBackgroundLayer(partTick: Float, mouseX: Int, mouseY: Int) {
    draw
    setContents
    tabsTop(1).enabled = hasBackpack
    tabsTop(2).enabled = hasKnife
    lastMouseX = mouseX
    lastMouseY = mouseY
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    mc.renderEngine.bindTexture(texture)
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize)
    if (this.func_146978_c(8, 35, 16, 16, mouseX, mouseY)) itemRender.renderItemIntoGUI(fontRendererObj, mc.renderEngine, new ItemStack(Objs.backpack), guiLeft + 8, guiTop + 35)
    if (this.func_146978_c(8, 53, 16, 16, mouseX, mouseY)) itemRender.renderItemIntoGUI(fontRendererObj, mc.renderEngine, new ItemStack(Objs.knife), guiLeft + 8, guiTop + 53)
    if (this.func_146978_c(196, 35, 16, 16, mouseX, mouseY)) itemRender.renderItemIntoGUI(fontRendererObj, mc.renderEngine, new ItemStack(Objs.lantern), guiLeft + 196, guiTop + 35)
    if (this.func_146978_c(196, 53, 16, 16, mouseX, mouseY)) itemRender.renderItemIntoGUI(fontRendererObj, mc.renderEngine, new ItemStack(Items.filled_map), guiLeft + 196, guiTop + 53)
    super.drawGuiContainerBackgroundLayer(partTick, mouseX, mouseY)
  }
  override def drawGuiContainerForegroundLayer(mouseX:Int, mouseY:Int) {
    if(active(1)==0)this.fontRendererObj.drawString(I18n.format("container.crafting", new java.lang.Object), 108, 16, 4210752)
  }
  override def drawScreen(mouseX:Int, mouseY:Int, partialTick:Float){
    super.drawScreen(mouseX, mouseY, partialTick)
    buttonList.foreach(button => if(button.isInstanceOf[ButtonCampinv])button.asInstanceOf[ButtonCampinv].drawHoverd(mouseX, mouseY))
  }
  def drawTab(left:Boolean, id:Int){
    if(id==1||id==2)mc.renderEngine.bindTexture(texture)
    getCleanGL
    if(id==0){ 
      this.mc.getTextureManager().bindTexture(inventoryTexture)
      this.drawTexturedModalRect(guiLeft + 29, guiTop+7, 7, 7, 154, 72);
      GuiInventory.func_147046_a(guiLeft + 51 + 22, guiTop + 75, 30, (guiLeft + 51 + 22 - lastMouseX).asInstanceOf[Float], (guiTop + 75 - 50 - lastMouseY).asInstanceOf[Float], this.mc.thePlayer);
    } else if(id==1)drawTexturedModalRect(guiLeft+29, guiTop+16, 29, 83, 162, 54)
    else if(id==2)drawTexturedModalRect(guiLeft+52, guiTop+16, 0, 166, 115, 54)
    else {
      
    }
  }
}

class GuiMapHUD extends GuiScreen {
  final val TEX_UTILS = new ResourceLocation(TextureInfo.GUI_UTILS)
  final val TEX_RED_DOT = new ResourceLocation(TextureInfo.RED_DOT)
  final val TEX_BLUE_DOT = new ResourceLocation(TextureInfo.BLUE_DOT)

  var colorData: HashMap[EntityPlayer, Array[Byte]] = new HashMap[EntityPlayer, Array[Byte]]()
  var posData: HashMap[EntityPlayer, Array[Int]] = new HashMap[EntityPlayer, Array[Int]]()
  private var rgbColors: Array[Int] = new Array[Int](16384)
  val textureID = GL11.glGenTextures()

  override def drawScreen(mouseX: Int, mouseY: Int, partTicks: Float) {
    GL11.glPushMatrix()
    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glScalef(0.5F, 0.5F, 0.5F)
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    mc.renderEngine.bindTexture(TEX_UTILS)
    drawTexturedModalRect((width * 2) - 133, 5, 0, 42, 128, 128)
    GL11.glScalef(2F, 2F, 2F)
    if (mc.thePlayer.worldObj.isRemote && (colorData != null) && (posData != null) && (colorData.contains(mc.thePlayer)) && (posData.contains(mc.thePlayer)) && (colorData(mc.thePlayer) != null) && (posData(mc.thePlayer) != null)) {
      for (i <- 0 until colorData(mc.thePlayer).length) {
        val colorIndex = colorData(mc.thePlayer)(i)
        if ((colorIndex / 4) == 0) rgbColors(i) = ((((i + (i / 128)) & 1) * 8) + 16) << 24
        else if (colorIndex >= 0) {
          val colorValue = MapColor.mapColorArray(colorIndex / 4).colorValue
          val heightFlag = colorIndex & 3
          var heigthDarkness = 220
          if (heightFlag == 2) heigthDarkness = 255
          if (heightFlag == 0) heigthDarkness = 180
          val Red = (((colorValue >> 16) & 255) * heigthDarkness) / 255
          val Green = (((colorValue >> 8) & 255) * heigthDarkness) / 255
          val Blue = ((colorValue & 255) * heigthDarkness) / 255
          rgbColors(i) = -16777216 | (Red << 16) | (Green << 8) | Blue
        }
      }
      val textureBuffer = BufferUtils.createByteBuffer(128 * 128 * 3)
      for (i <- 0 until (128 * 128)) {
        val pixel = rgbColors(i)
        val color = Color.decode(java.lang.Integer.toString(pixel))
        textureBuffer.put(color.getRed.toByte)
        textureBuffer.put(color.getGreen.toByte)
        textureBuffer.put(color.getBlue.toByte)
      }
      textureBuffer.flip()
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
      GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 128, 128, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, textureBuffer)
      GL11.glBegin(GL11.GL_QUADS)
      GL11.glTexCoord2f(0f, 0f)
      GL11.glVertex2f(width - (126f / 2), 12f / 2)
      GL11.glTexCoord2f(0f, 1f)
      GL11.glVertex2f(width - (126f / 2), 126f / 2)
      GL11.glTexCoord2f(1f, 1f)
      GL11.glVertex2f(width - (12 / 2), 126f / 2)
      GL11.glTexCoord2f(1f, 0f)
      GL11.glVertex2f(width - (12f / 2), 12f / 2)
      GL11.glEnd()
      for (i <- 0 until mc.theWorld.playerEntities.size) {
        val player = mc.theWorld.playerEntities.get(i).asInstanceOf[EntityPlayer]
        val scale = (57F / (64F * (Math.pow(2, posData(mc.thePlayer)(0))))).toFloat
        var xDivision = (scale * (player.posX - posData(mc.thePlayer)(1))).toInt
        var zDivision = (scale * (player.posZ - posData(mc.thePlayer)(2))).toInt
        if (xDivision > 57) xDivision = 57
        else if (xDivision < -57) xDivision = -57
        if (zDivision > 57) zDivision = 57
        else if (zDivision < -57) zDivision = -57
        if (mc.thePlayer == player) mc.renderEngine.bindTexture(TEX_RED_DOT)
        else mc.renderEngine.bindTexture(TEX_BLUE_DOT)

        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0f, 0f)
        GL11.glVertex2f(((width - (69 / 2)) + (xDivision / 2)) - 3, ((69 / 2) + (zDivision / 2)) - 3)
        GL11.glTexCoord2f(0f, 1f)
        GL11.glVertex2f(((width - (69 / 2)) + (xDivision / 2)) - 3, (69 / 2) + (zDivision / 2) + 3)
        GL11.glTexCoord2f(1f, 1f)
        GL11.glVertex2f((width - (69 / 2)) + (xDivision / 2) + 3, (69 / 2) + (zDivision / 2) + 3)
        GL11.glTexCoord2f(1f, 0f)
        GL11.glVertex2f((width - (69 / 2)) + (xDivision / 2) + 3, ((69 / 2) + (zDivision / 2)) - 3)
        GL11.glEnd()
      }
    }
    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
    GL11.glPopMatrix()
  }
}