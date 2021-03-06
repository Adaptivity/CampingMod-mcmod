package com.rikmuld.camping.common.network

import java.nio.ByteBuffer
import com.rikmuld.camping.CampingMod
import com.rikmuld.camping.common.objs.tile.TileEntityTent
import com.rikmuld.camping.common.objs.tile.TileEntityWithBounds
import com.rikmuld.camping.core.NBTInfo
import com.rikmuld.camping.core.Objs
import com.rikmuld.camping.misc.Bounds
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import com.rikmuld.corerm.common.network.BasicPacketData
import com.rikmuld.corerm.common.objs.tile.TileEntityWithInventory
import com.rikmuld.camping.common.objs.tile.TileEntityLog
import com.rikmuld.camping.common.inventory.gui.ContainerTabbed
import net.minecraft.util.ResourceLocation
import java.io.File
import net.minecraft.client.Minecraft

class OpenGui(var id: Int) extends BasicPacketData {
  var x: Int = 0
  var y: Int = 0
  var z: Int = 0

  def this() = this(0);
  def this(id: Int, x: Int, y: Int, z: Int) {
    this(id)
    this.x = x
    this.y = y
    this.z = z
  }
  override def setData(stream: PacketBuffer) {
    stream.writeInt(id)
    stream.writeInt(x)
    stream.writeInt(y)
    stream.writeInt(z)
  }
  override def getData(stream: PacketBuffer) {
    id = stream.readInt
    x = stream.readInt
    y = stream.readInt
    z = stream.readInt
  }
  override def handlePacket(player: EntityPlayer, ctx: MessageContext) = player.openGui(CampingMod, id, player.worldObj, x, y, z)
}

class NBTPlayer(var tag: NBTTagCompound) extends BasicPacketData {
  def this() = this(null);
  override def setData(stream: PacketBuffer) = stream.writeNBTTagCompoundToBuffer(tag)
  override def getData(stream: PacketBuffer) = tag = stream.readNBTTagCompoundFromBuffer()
  override def handlePacket(player: EntityPlayer, ctx: MessageContext) = player.getEntityData.setTag(NBTInfo.INV_CAMPING, tag)
}

class Map(var scale: Int, var x: Int, var z: Int, var colours: Array[Byte]) extends BasicPacketData {
  def this() = this(0, 0, 0, null)
  override def setData(stream: PacketBuffer) {
    stream.writeInt(x)
    stream.writeInt(z)
    stream.writeInt(scale)
    stream.writeBytes(colours)
  }
  override def getData(stream: PacketBuffer) {
    x = stream.readInt()
    z = stream.readInt()
    scale = stream.readInt()
    colours = Array.ofDim[Byte](16384)
    stream.readBytes(colours)
  }
  override def handlePacket(player: EntityPlayer, ctx: MessageContext) {
    if (Objs.eventsClient.map != null) {
      Objs.eventsClient.map.colorData(player) = colours
      Objs.eventsClient.map.posData(player) = Array(scale, x, z)
    }
  }
}

class Items(var slot: Int, var x: Int, var y: Int, var z: Int, var stack: ItemStack) extends BasicPacketData {
  def this() = this(0, 0, 0, 0, null)
  override def setData(stream: PacketBuffer) {
    stream.writeInt(slot)
    stream.writeInt(x)
    stream.writeInt(y)
    stream.writeInt(z)
    stream.writeItemStackToBuffer(stack)
  }
  override def getData(stream: PacketBuffer) {
    slot = stream.readInt
    x = stream.readInt
    y = stream.readInt
    z = stream.readInt
    stack = stream.readItemStackFromBuffer()
  }
  override def handlePacket(player: EntityPlayer, ctx: MessageContext) {
    if (player.worldObj.getTileEntity(x, y, z) != null) {
      player.worldObj.getTileEntity(x, y, z).asInstanceOf[TileEntityWithInventory].setInventorySlotContents(slot, stack)
    }
  }
}

class BoundsData(var bounds: Bounds, var x: Int, var y: Int, var z: Int) extends BasicPacketData {
  var xMin = if (bounds != null) bounds.xMin else 0
  var yMin = if (bounds != null) bounds.yMin else 0
  var zMin = if (bounds != null) bounds.zMin else 0
  var xMax = if (bounds != null) bounds.xMax else 0
  var yMax = if (bounds != null) bounds.yMax else 0
  var zMax = if (bounds != null) bounds.zMax else 0

  def this() = this(null, 0, 0, 0)
  override def getData(stream: PacketBuffer) {
    x = stream.readInt
    y = stream.readInt
    z = stream.readInt
    xMin = stream.readFloat
    yMin = stream.readFloat
    zMin = stream.readFloat
    xMax = stream.readFloat
    yMax = stream.readFloat
    zMax = stream.readFloat
  }
  override def setData(stream: PacketBuffer) {
    stream.writeInt(x)
    stream.writeInt(y)
    stream.writeInt(z)
    stream.writeFloat(xMin)
    stream.writeFloat(yMin)
    stream.writeFloat(zMin)
    stream.writeFloat(xMax)
    stream.writeFloat(yMax)
    stream.writeFloat(zMax)
  }
  override def handlePacket(player: EntityPlayer, ctx: MessageContext) {
    if (player.worldObj.getTileEntity(x, y, z) != null) {
      player.worldObj.getTileEntity(x, y, z).asInstanceOf[TileEntityWithBounds].bounds = new Bounds(xMin, yMin, zMin, xMax, yMax, zMax)
    }
  }
}

class PlayerSleepInTent(var x: Int, var y: Int, var z: Int) extends BasicPacketData {
  def this() = this(0, 0, 0)

  override def handlePacket(player: EntityPlayer, ctx: MessageContext) = player.worldObj.getTileEntity(x, y, z).asInstanceOf[TileEntityTent].sleep(player)
  override def getData(stream: PacketBuffer) {
    x = stream.readInt
    y = stream.readInt
    z = stream.readInt
  }
  override def setData(stream: PacketBuffer) {
    stream.writeInt(x)
    stream.writeInt(y)
    stream.writeInt(z)
  }
}

class PlayerExitLog(var x: Int, var y: Int, var z: Int) extends BasicPacketData {
  def this() = this(0, 0, 0)
 
  override def handlePacket(player: EntityPlayer, ctx: MessageContext) {
    if(!player.worldObj.isRemote&&player.worldObj.getBlock(x, y, z)==Objs.log&&player.worldObj.getTileEntity(x, y, z).asInstanceOf[TileEntityLog].mountable.riddenByEntity!=null){
      player.worldObj.getTileEntity(x, y, z).asInstanceOf[TileEntityLog].mountable.riddenByEntity.mountEntity(null) 
      player.worldObj.getTileEntity(x, y, z).asInstanceOf[TileEntityLog].mountable.player = null;
    }
  }
  override def getData(stream: PacketBuffer) {
    x = stream.readInt
    y = stream.readInt
    z = stream.readInt
  }
  override def setData(stream: PacketBuffer) {
    stream.writeInt(x)
    stream.writeInt(y)
    stream.writeInt(z)
  }
}

class KeyData(var id: Int) extends BasicPacketData {
  def this() = this(0)
  override def setData(stream: PacketBuffer) = stream.writeInt(id)
  override def getData(stream: PacketBuffer) = id = stream.readInt
  override def handlePacket(player: EntityPlayer, ctx: MessageContext) = Objs.events.keyPressedServer(player, id)
}