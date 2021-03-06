package com.rikmuld.camping.common.objs.entity

import net.minecraft.entity.EntityCreature
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.EntityAIAttackOnCollide
import net.minecraft.entity.ai.EntityAINearestAttackableTarget
import net.minecraft.entity.ai.EntityAISwimming
import net.minecraft.entity.ai.EntityAIWander
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraft.entity.EntityLivingBase
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.util.MathHelper
import net.minecraft.util.DamageSource
import net.minecraft.entity.ai.EntityAIWatchClosest2
import net.minecraft.init.Items
import net.minecraft.entity.IMerchant
import net.minecraft.village.MerchantRecipe
import net.minecraft.item.ItemStack
import net.minecraft.village.MerchantRecipeList
import cpw.mods.fml.relauncher.SideOnly
import cpw.mods.fml.relauncher.Side
import net.minecraft.entity.INpc
import scala.collection.mutable.HashMap
import net.minecraft.util.Tuple
import net.minecraft.item.Item
import com.rikmuld.camping.core.Objs
import java.util.Random
import java.util.ArrayList
import com.rikmuld.camping.core.PartInfo

object Camper {
  val recipeListRaw = new HashMap[Item, Tuple]()

  recipeListRaw(Objs.venisonCooked) = new Tuple(Integer.valueOf(-4), Integer.valueOf(1))
  recipeListRaw(Objs.knife) = new Tuple(Integer.valueOf(1), Integer.valueOf(2))
  recipeListRaw(Objs.backpack) = new Tuple(Integer.valueOf(1), Integer.valueOf(2))
  recipeListRaw(Objs.furLeg) = new Tuple(Integer.valueOf(4), Integer.valueOf(6))
  recipeListRaw(Objs.furHead) = new Tuple(Integer.valueOf(3), Integer.valueOf(5))
  recipeListRaw(Objs.furChest) = new Tuple(Integer.valueOf(5), Integer.valueOf(8))
  recipeListRaw(Objs.furBoot) = new Tuple(Integer.valueOf(2), Integer.valueOf(4))
  recipeListRaw(Objs.parts) = new Tuple(Integer.valueOf(1), Integer.valueOf(2))
}

class Camper(world: World) extends EntityCreature(world) with IMerchant with INpc {
  setGender(rand.nextInt(2))
  setSize(0.6F, 1.8F)
  getNavigator().setAvoidsWater(true)
  tasks.addTask(1, new EntityAISwimming(this))
  tasks.addTask(2, new EntityAIAttackOnCollide(this, classOf[Bear], 1.0F, false))
  tasks.addTask(3, new EntityAIAttackOnCollide(this, classOf[EntityMob], 1.0F, false))
  tasks.addTask(4, new EntityAIWatchClosest2(this, classOf[EntityPlayer], 3.0F, 1.0F))
  tasks.addTask(6, new EntityAIWander(this, 0.6D))
  targetTasks.addTask(0, new EntityAINearestAttackableTarget(this, classOf[EntityMob], 0, false))
  targetTasks.addTask(0, new EntityAINearestAttackableTarget(this, classOf[Bear], 0, false))

  var xHome, yHome, zHome: Int = _
  var hasHomeCoords = false
  var playerBuy: EntityPlayer = null
  var recipeList: MerchantRecipeList = _

  def this(world: World, x: Double, y: Double, z: Double) {
    this(world)
    setPosition(x, y, z)
    xHome = x.toInt
    yHome = y.toInt
    zHome = z.toInt

    hasHomeCoords = true
  }
  def setGender(gender: Int) = dataWatcher.updateObject(16, Integer.valueOf(gender))
  def getGender: Int = dataWatcher.getWatchableObjectInt(16)
  override def writeEntityToNBT(tag: NBTTagCompound) {
    super.writeEntityToNBT(tag)
    tag.setInteger("gender", getGender)
    tag.setInteger("xHome", xHome)
    tag.setInteger("yHome", yHome)
    tag.setInteger("zHome", zHome)
    tag.setBoolean("hasHome", hasHomeCoords)
  }
  override def readEntityFromNBT(tag: NBTTagCompound) {
    super.writeEntityToNBT(tag)
    setGender(tag.getInteger("gender"))
    xHome = tag.getInteger("xHome")
    yHome = tag.getInteger("yHome")
    zHome = tag.getInteger("zHome")
    hasHomeCoords = tag.getBoolean("hasHome")
  }
  protected override def applyEntityAttributes {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.4D)
    getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0D)
  }
  override def canDespawn = false
  override def entityInit {
    super.entityInit
    dataWatcher.addObject(16, Integer.valueOf(0))
  }
  protected override def getDeathSound = "mob.villager.death"
  protected override def getHurtSound = "mob.villager.hit"
  protected override def getLivingSound = "mob.villager.idle"
  override def isAIEnabled = true
  override def attackEntityAsMob(entity: Entity): Boolean = {
    var f = this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue.toFloat
    var i = 0
    if (entity.isInstanceOf[EntityLivingBase]) {
      f += EnchantmentHelper.getEnchantmentModifierLiving(this, entity.asInstanceOf[EntityLivingBase])
      i += EnchantmentHelper.getKnockbackModifier(this, entity.asInstanceOf[EntityLivingBase])
    }
    val flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), f)
    if (flag) {
      if (i > 0) {
        entity.addVelocity((-MathHelper.sin(this.rotationYaw * Math.PI.toFloat / 180.0F) * i.toFloat * 0.5F).toDouble, 0.1D, (MathHelper.cos(this.rotationYaw * Math.PI.toFloat / 180.0F) * i.toFloat * 0.5F).toDouble)
        this.motionX *= 0.6D
        this.motionZ *= 0.6D
      }
      val j = EnchantmentHelper.getFireAspectModifier(this)
      if (j > 0) entity.setFire(j * 4)
      if (entity.isInstanceOf[EntityLivingBase]) EnchantmentHelper.func_151384_a(entity.asInstanceOf[EntityLivingBase], this)
      EnchantmentHelper.func_151385_b(this, entity)
    }
    flag
  }
  override protected def attackEntity(entity: Entity, num: Float) {
    if (this.attackTime <= 0 && num < 2.0F && entity.boundingBox.maxY > this.boundingBox.minY && entity.boundingBox.minY < this.boundingBox.maxY) {
      this.attackTime = 20
      this.attackEntityAsMob(entity)
    }
  }
  override def onUpdate() {
    super.onUpdate()
    if (rand.nextInt(500) == 0 && (!worldObj.isRemote)) {
      getNavigator.tryMoveToXYZ(xHome - 2 + rand.nextInt(4), yHome, zHome - 2 + rand.nextInt(4), 0.4)
    }
  }
  override def interact(player: EntityPlayer): Boolean = {
    val itemstack = player.inventory.getCurrentItem
    val flag = (itemstack != null) && (itemstack.getItem == Items.spawn_egg)
    if (!flag && isEntityAlive && !isTrading && !player.isSneaking) {
      if (!worldObj.isRemote) {
        setCustomer(player)
        player.displayGUIMerchant(this, "Camper")
      }
      true
    } else super.interact(player)
  }
  def setCustomer(player: EntityPlayer) = playerBuy = player
  def getCustomer(): EntityPlayer = playerBuy
  def getRecipes(player: EntityPlayer): MerchantRecipeList = if (recipeList == null) setRecipeList else recipeList
  @SideOnly(Side.CLIENT)
  def setRecipes(recipes: MerchantRecipeList) {

  }
  def useRecipe(resipes: MerchantRecipe) {

  }
  def func_110297_a_(stack: ItemStack) {

  }
  def isTrading: Boolean = getCustomer != null
  def setRecipeList: MerchantRecipeList = {
    recipeList = new MerchantRecipeList()
    if (getGender == 0) {
      addBlacksmithItem(recipeList, Objs.furBoot, 0, rand, 0.4F)
      addBlacksmithItem(recipeList, Objs.furChest, 0, rand, 0.3F)
      addBlacksmithItem(recipeList, Objs.furHead, 0, rand, 0.4F)
      addBlacksmithItem(recipeList, Objs.furLeg, 0, rand, 0.4F)
      addBlacksmithItem(recipeList, Objs.knife, 0, rand, 0.6F)
      addBlacksmithItem(recipeList, Objs.backpack, 0, rand, 0.5F)
    } else {
      addBlacksmithItem(recipeList, Objs.venisonCooked, 0, rand, 0.6F)
      addBlacksmithItem(recipeList, Objs.parts, PartInfo.PAN, rand, 0.6F)
    }
    if (recipeList.isEmpty()) {
      addBlacksmithItem(recipeList, Objs.knife, 0, rand, 0.6F)
    }
    recipeList
  }
  def addBlacksmithItem(merchantRecipeList: MerchantRecipeList, item: Item, meta: Int, random: Random, par3: Float) {
    if (random.nextFloat() < par3) {
      val j = randomCount(item, random)
      var itemstack: ItemStack = null
      var itemstack1: ItemStack = null
      if (j < 0) {
        itemstack = new ItemStack(Items.emerald, 1, 0)
        itemstack1 = new ItemStack(item, -j, meta)
      } else {
        itemstack = new ItemStack(Items.emerald, j, 0)
        itemstack1 = new ItemStack(item, 1, meta)
      }
      merchantRecipeList.asInstanceOf[ArrayList[MerchantRecipe]].add(new MerchantRecipe(itemstack, itemstack1))
    }
  }
  private def randomCount(item: Item, random: Random): Int = {
    val tuple = Camper.recipeListRaw(item).asInstanceOf[Tuple]
    if (tuple == null) 1 else (if (tuple.getFirst.asInstanceOf[java.lang.Integer].intValue() >= tuple.getSecond.asInstanceOf[java.lang.Integer].intValue()) tuple.getFirst.asInstanceOf[java.lang.Integer].intValue() else tuple.getFirst.asInstanceOf[java.lang.Integer].intValue() + random.nextInt(tuple.getSecond.asInstanceOf[java.lang.Integer].intValue() - tuple.getFirst.asInstanceOf[java.lang.Integer].intValue()))
  }
}