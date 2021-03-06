/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.FurryKingdoms.Base;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public abstract class EntityCharacterBase extends EntityLiving {

	protected SpeciesTypes species;

	public EntityCharacterBase(World par1World) {
		super(par1World);
	}

	@Override
	public int getMaxHealth() {
		return species.getMaxHealth();
	}

	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, int par2) {
		if (ForgeHooks.onLivingAttack(this, par1DamageSource, par2))
			return false;

		if (this.isEntityInvulnerable())
			return false;
		else if (worldObj.isRemote)
			return false;
		else {
			entityAge = 0;

			if (health <= 0)
				return false;
			else if (par1DamageSource.isFireDamage() && this.isPotionActive(Potion.fireResistance))
				return false;
			else {
				if ((par1DamageSource == DamageSource.anvil || par1DamageSource == DamageSource.fallingBlock) && this.getCurrentItemOrArmor(4) != null) {
					this.getCurrentItemOrArmor(4).damageItem(par2 * 4 + rand.nextInt(par2 * 2), this);
					par2 = (int)(par2 * 0.75F);
				}

				limbYaw = 1.5F;
				boolean flag = true;

				if (hurtResistantTime > maxHurtResistantTime / 2.0F) {
					if (par2 <= lastDamage)
						return false;

					this.damageEntity(par1DamageSource, par2 - lastDamage);
					lastDamage = par2;
					flag = false;
				}
				else {
					lastDamage = par2;
					prevHealth = health;
					hurtResistantTime = maxHurtResistantTime;
					this.damageEntity(par1DamageSource, (int)(par2*species.getHurtability()));
					hurtTime = maxHurtTime = 10;
				}

				attackedAtYaw = 0.0F;
				Entity entity = par1DamageSource.getEntity();

				if (entity != null) {
					if (entity instanceof EntityLiving)
						this.setRevengeTarget((EntityLiving)entity);

					if (entity instanceof EntityPlayer) {
						recentlyHit = 100;
						attackingPlayer = (EntityPlayer)entity;
					}
					else if (entity instanceof EntityWolf) {
						EntityWolf entitywolf = (EntityWolf)entity;

						if (entitywolf.isTamed()) {
							recentlyHit = 100;
							attackingPlayer = null;
						}
					}
				}

				if (flag) {
					worldObj.setEntityState(this, (byte)2);

					if (par1DamageSource != DamageSource.drown)
						this.setBeenAttacked();

					if (entity != null) {
						double d0 = entity.posX - posX;
						double d1;

						for (d1 = entity.posZ - posZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D)
							d0 = (Math.random() - Math.random()) * 0.01D;

						attackedAtYaw = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - rotationYaw;
						this.knockBack(entity, par2, d0, d1);
					}
					else
						attackedAtYaw = (int)(Math.random() * 2.0D) * 180;
				}

				if (health <= 0) {
					if (flag)
						this.playSound(this.getDeathSound(), this.getSoundVolume(), this.getSoundPitch());
					this.onDeath(par1DamageSource);
				}
				else if (flag)
					this.playSound(this.getHurtSound(), this.getSoundVolume(), this.getSoundPitch());

				return true;
			}
		}
	}

}
