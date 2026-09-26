package com.jhonswolf.fantasticpets.entity;

import com.jhonswolf.fantasticpets.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

// Define a coruja como um Animal capaz de voar e com animações GeckoLib
public class OwlEntity extends TamableAnimal implements GeoEntity, FlyingAnimal {

    // Cache interno do GeckoLib para processar os movimentos suavemente
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Variável sincronizada para mostrar a carta nas garras da coruja
    private static final EntityDataAccessor<ItemStack> PACOTE =
            SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.ITEM_STACK);

    private String nomeDoAlvo = null;
    private boolean emMissao = false;

    public OwlEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
    }

    // Configura os atributos iniciais da coruja
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D) // 10 de vida = 5 corações
                .add(Attributes.FLYING_SPEED, 0.6D) // Rapidez a voar
                .add(Attributes.MOVEMENT_SPEED, 0.2D); // Rapidez a andar no chão
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PACOTE, ItemStack.EMPTY);
    }

    public ItemStack getPacoteCarregado() {
        return this.entityData.get(PACOTE);
    }

    public void setPacoteCarregado(ItemStack stack) {
        this.entityData.set(PACOTE, stack);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemNaMao = player.getItemInHand(hand);

        // Dieta exclusiva da Coruja: Frango Cru (domar/curar/crescer) | Coelho Cru (reproduzir)
        Item itemDomacaoCura = Items.CHICKEN;
        Item itemReproducao = Items.RABBIT;

        // 1. Domação, Cura ou Crescimento
        if (itemNaMao.getItem() == itemDomacaoCura) {
            if (this.isBaby()) {
                if (!player.level().isClientSide) {
                    if (!player.getAbilities().instabuild) itemNaMao.shrink(1);
                    this.ageUp(60, true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            } else if (!this.isTame()) {
                if (!player.level().isClientSide) {
                    if (!player.getAbilities().instabuild) itemNaMao.shrink(1);

                    if (this.random.nextInt(3) == 0) {
                        this.tame(player);
                        this.navigation.stop();
                        this.setOrderedToSit(true);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            } else if (this.getHealth() < this.getMaxHealth()) {
                if (!player.level().isClientSide) {
                    if (!player.getAbilities().instabuild) itemNaMao.shrink(1);
                    this.heal(4.0F);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }

        // 2. Interações do Dono
        if (this.isTame() && this.isOwnedBy(player)) {

            // Atualizado para verificar a nossa nova Carta Selada
            boolean isCorrespondencia = itemNaMao.getItem() == ModItems.SEALED_SCROLL.get();

            // A) REPRODUÇÃO
            if (itemNaMao.getItem() == itemReproducao) {
                if (!this.isBaby() && !this.isInLove()) {
                    if (!player.level().isClientSide) {
                        if (!player.getAbilities().instabuild) {
                            itemNaMao.shrink(1);
                        }
                        this.setInLove(player);
                        this.level().broadcastEntityEvent(this, (byte) 18);
                    }
                    return InteractionResult.sidedSuccess(player.level().isClientSide);
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }

            // B) Entregar a encomenda (A coruja aceita tanto pacotes quanto pergaminhos)
            if (this.getPacoteCarregado().isEmpty() && isCorrespondencia) {
                if (!player.level().isClientSide) {
                    ItemStack copia = itemNaMao.copy();
                    copia.setCount(1);
                    this.setPacoteCarregado(copia);
                    this.lerDestinatarioDaEncomenda();
                    this.setOrderedToSit(false);
                }
                if (!player.getAbilities().instabuild) itemNaMao.shrink(1);
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // C) Pegar a encomenda de volta
            else if (!this.getPacoteCarregado().isEmpty() && itemNaMao.isEmpty()) {
                if (!player.level().isClientSide) {
                    player.addItem(this.getPacoteCarregado());
                    this.setPacoteCarregado(ItemStack.EMPTY);
                    player.displayClientMessage(Component.literal("§eCoruja: Devolvi a carta."), true);
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // D) Comando de Sentar/Seguir
            else if (!player.isShiftKeyDown() && this.getPacoteCarregado().isEmpty() && !isCorrespondencia && itemNaMao.isEmpty()) {
                if (!player.level().isClientSide) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    if (this.isOrderedToSit()) {
                        this.navigation.stop();
                        player.displayClientMessage(Component.literal("§eCoruja: A aguardar (Sentado)."), true);
                    } else {
                        player.displayClientMessage(Component.literal("§eCoruja: A seguir-te."), true);
                    }
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // E) Subir no ombro
            else if (player.isShiftKeyDown() && this.getPacoteCarregado().isEmpty() && !isCorrespondencia && itemNaMao.isEmpty()) {
                if (!player.level().isClientSide) {
                    this.setOrderedToSit(false);
                    if (this.setEntityOnShoulder(player)) {
                        player.displayClientMessage(Component.literal("§eCoruja: Às ordens!"), true);
                    }
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    private void lerDestinatarioDaEncomenda() {
        ItemStack pacote = this.getPacoteCarregado();
        if (!pacote.isEmpty() && pacote.hasTag()) {
            CompoundTag tag = pacote.getTag();

            // Atualizado para ler o "Recipient" do nosso SealedScrollC2SPacket
            if (tag != null && tag.contains("Recipient")) {
                this.nomeDoAlvo = tag.getString("Recipient");
                this.emMissao = true;

                if (this.getOwner() instanceof Player dono) {
                    dono.displayClientMessage(Component.literal("§aCoruja: Destinatário " + this.nomeDoAlvo + " identificado. A iniciar entrega!"), true);
                }
            } else {
                if (this.getOwner() instanceof Player dono) {
                    dono.displayClientMessage(Component.literal("§cCoruja: Esta carta não tem um destinatário válido."), true);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.getPacoteCarregado().isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            this.getPacoteCarregado().save(itemTag);
            tag.put("PacoteCarregado", itemTag);
        }
        tag.putBoolean("EmMissao", this.emMissao);
        if (this.nomeDoAlvo != null) {
            tag.putString("NomeDoAlvo", this.nomeDoAlvo);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PacoteCarregado")) {
            CompoundTag itemTag = tag.getCompound("PacoteCarregado");
            this.setPacoteCarregado(ItemStack.of(itemTag));
        }
        this.emMissao = tag.getBoolean("EmMissao");
        if (tag.contains("NomeDoAlvo")) {
            this.nomeDoAlvo = tag.getString("NomeDoAlvo");
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new RotinaDeEntregaGoal());
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.25D, Ingredient.of(Items.CHICKEN), false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navegador = new FlyingPathNavigation(this, level);
        navegador.setCanOpenDoors(false);
        navegador.setCanFloat(true);
        navegador.setCanPassDoors(true);
        return navegador;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        if (otherAnimal == this) return false;
        return otherAnimal instanceof OwlEntity && super.canMate(otherAnimal);
    }

    // Documentação: Controla o nascimento/reprodução. Agora gera entre 1 a 3 Ovos de Coruja aleatoriamente.
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        // Gera um número aleatório entre 1 e 3 (nextInt(3) dá 0, 1 ou 2; somamos 1 para dar 1, 2 ou 3)
        int quantidadeOvos = this.random.nextInt(3) + 1;

        // Cria o lote de ovos com a quantidade sorteada
        ItemStack eggStack = new ItemStack(ModItems.OWL_EGG.get(), quantidadeOvos);

        // Cria a entidade de item no mundo nas coordenadas exatas da coruja
        ItemEntity itemEntity = new ItemEntity(
                serverLevel,
                this.getX(),
                this.getY(),
                this.getZ(),
                eggStack
        );

        // Adiciona uma pequena velocidade de dispersão para os ovos saltarem ligeiramente ao cair
        itemEntity.setDeltaMovement(
                (this.random.nextDouble() - 0.5) * 0.1,
                0.2,
                (this.random.nextDouble() - 0.5) * 0.1
        );

        serverLevel.addFreshEntity(itemEntity);

        // Retorna null porque o resultado da reprodução é um item (o ovo) e não uma entidade viva imediata
        return null;
    }

    // Documentação: Controla o estado inicial da coruja ao nascer no mundo
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);

        // Se a coruja estiver a nascer de forma natural no mundo (por exemplo, na Taiga)
        if (spawnType == MobSpawnType.NATURAL && this.random.nextFloat() < 0.10F) {
            // 10% de chance de nascer como bebé (cria)
            this.setBaby(true);
        }

        return spawnGroupData;
    }

    // Confirma ao jogo que esta entidade pode estar no ar
    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    private boolean setEntityOnShoulder(Player player) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", this.getEncodeId());
        this.saveWithoutId(tag);
        if (player.setEntityOnShoulder(tag)) {
            this.discard();
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PARROT_AMBIENT; // Som provisório
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.PARROT_HURT; // Som provisório
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH; // Som provisório
    }

    @Override
    protected float getSoundVolume() {
        return 0.5F;
    }

    // Documentação: Regista o controlador que decide que animação tocar com base no estado da coruja
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        // Cria um controlador com 5 ticks de transição suave
        controllers.add(new AnimationController<>(this, "controller", 5, event -> {

            // 1. Se a coruja está no ar, toca a animação "fly" que criaste no Blockbench
            if (this.isFlying()) {
                // O nome "fly" tem de ser exatamente igual ao que está no ficheiro owl.animation.json
                return event.setAndContinue(RawAnimation.begin().thenLoop("fly"));
            }

            // 2. Para todos os outros estados (como ainda não tens as animações),
            // dizemos ao jogo para não tocar nada. A coruja ficará na pose padrão.
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // Documentação: Inteligência Artificial (IA) para a entrega cinematográfica de cartas
    class RotinaDeEntregaGoal extends Goal {
        private int fase = 0;
        private int tempo = 0;
        private Player jogadorAlvo = null;
        private net.minecraft.world.entity.LivingEntity dono = null;

        public RotinaDeEntregaGoal() {
            // Diz ao Minecraft que este objetivo controla o movimento (MOVE) da entidade
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return emMissao && nomeDoAlvo != null && !OwlEntity.this.getPacoteCarregado().isEmpty();
        }

        @Override
        public boolean canContinueToUse() {
            return emMissao;
        }

        @Override
        public void start() {
            this.fase = 0;
            this.tempo = 0;
            OwlEntity.this.setNoGravity(true); // Desativa a gravidade para podermos manipular o voo
        }

        @Override
        public void tick() {
            this.tempo++;

            if (fase == 0) {
                // FASE 0: Decolagem Diagonal. Calcula a "frente" da coruja.
                // Converte a rotação (Yaw) de graus para radianos
                float yaw = OwlEntity.this.getYRot() * ((float)Math.PI / 180F);

                // Calcula a velocidade para a frente usando trigonometria
                double velocidadeFrente = 0.2;
                double frenteX = -Math.sin(yaw) * velocidadeFrente;
                double frenteZ = Math.cos(yaw) * velocidadeFrente;

                // Define o movimento diagonal: Frente nos eixos X/Z, Subida no eixo Y (0.25)
                OwlEntity.this.setDeltaMovement(frenteX, 0.25, frenteZ);

                // Após cerca de 3 segundos (60 ticks), passa para a próxima fase
                if (tempo > 60) {
                    fase = 1;
                }
            }
            else if (fase == 1) {
                // FASE 1: Viagem rápida invisível. Procura o jogador no servidor inteiro.
                this.jogadorAlvo = OwlEntity.this.level().players().stream()
                        .filter(p -> p.getName().getString().equals(nomeDoAlvo))
                        .findFirst().orElse(null);

                if (jogadorAlvo != null) {
                    // CÁLCULO DO MERGULHO: Gera um ângulo aleatório para a coruja chegar de qualquer direção
                    double anguloChegada = OwlEntity.this.random.nextDouble() * 2 * Math.PI;
                    double distanciaHorizontal = 30.0; // Distância horizontal da ave para o alvo

                    // Converte o ângulo em coordenadas X e Z (Desvio)
                    double offsetX = Math.cos(anguloChegada) * distanciaHorizontal;
                    double offsetZ = Math.sin(anguloChegada) * distanciaHorizontal;

                    // Posiciona a coruja a 30 blocos de distância e 30 blocos de altura
                    OwlEntity.this.setPos(jogadorAlvo.getX() + offsetX, jogadorAlvo.getY() + 30, jogadorAlvo.getZ() + offsetZ);
                    fase = 2;
                } else {
                    if (OwlEntity.this.getOwner() instanceof Player jogadorDono) {
                        jogadorDono.displayClientMessage(Component.literal("§cCoruja: Não encontrei o destinatário no servidor. A regressar!"), true);
                    }
                    fase = 5; // Salta para a fase de regresso
                }
            }
            else if (fase == 2) {
                // NOVA PROTEÇÃO: Verifica se o alvo desconectou a meio do mergulho
                if (this.jogadorAlvo == null || this.jogadorAlvo.isRemoved()) {
                    if (OwlEntity.this.getOwner() instanceof Player jogadorDono) {
                        jogadorDono.displayClientMessage(Component.literal("§cCoruja: O destinatário desapareceu de repente! A abortar entrega."), true);
                    }
                    fase = 5; // Salta para o regresso
                    return;   // Sai do tick atual para evitar calcular coordenadas de um alvo fantasma
                }

                // CÁLCULO DINÂMICO: Verifica a distância real para acelerar se o jogador estiver a fugir
                double distanciaSqr = OwlEntity.this.distanceToSqr(jogadorAlvo);
                double velocidade = distanciaSqr > 100.0 ? 0.7 : 0.3; // Acelera se estiver a mais de 10 blocos de distância

                // FASE 2: Mergulho Diagonal na direção da entrega
                double distX = jogadorAlvo.getX() - OwlEntity.this.getX();
                double distY = (jogadorAlvo.getY() + 1.0) - OwlEntity.this.getY();
                double distZ = jogadorAlvo.getZ() - OwlEntity.this.getZ();

                net.minecraft.world.phys.Vec3 direcao = new net.minecraft.world.phys.Vec3(distX, distY, distZ).normalize().scale(0.3);
                OwlEntity.this.setDeltaMovement(direcao);

                // OBRIGA a coruja a olhar para o alvo enquanto desce (ajusta o modelo 3D)
                OwlEntity.this.getLookControl().setLookAt(jogadorAlvo, 30.0F, 30.0F);

                // Se a coruja chegar a menos de 3 blocos do jogador, avança para largar a carta
                if (OwlEntity.this.distanceTo(jogadorAlvo) < 3.0F) {
                    fase = 3;
                }
            }
            else if (fase == 3) {
                // FASE 3: Entrega. Larga a carta no mundo físico.
                ItemEntity itemDrop = new ItemEntity(
                        OwlEntity.this.level(),
                        OwlEntity.this.getX(),
                        OwlEntity.this.getY(),
                        OwlEntity.this.getZ(),
                        OwlEntity.this.getPacoteCarregado().copy()
                );
                OwlEntity.this.level().addFreshEntity(itemDrop);
                OwlEntity.this.setPacoteCarregado(ItemStack.EMPTY); // Esvazia as garras

                jogadorAlvo.sendSystemMessage(Component.literal("§eCoruja: Uma carta chegou para si!"));

                this.tempo = 0; // Reinicia o cronómetro
                fase = 4;
            }
            else if (fase == 4) {
                // FASE 4: Fuga. A coruja volta a subir para os céus rapidamente.
                float yaw = OwlEntity.this.getYRot() * ((float)Math.PI / 180F);

                double velocidadeFrente = 0.2;
                double frenteX = -Math.sin(yaw) * velocidadeFrente;
                double frenteZ = Math.cos(yaw) * velocidadeFrente;

                OwlEntity.this.setDeltaMovement(frenteX, 0.25, frenteZ);
                if (this.tempo > 60) {
                    fase = 5;
                }
            }
            else if (fase == 5) {
                // FASE 5: Regresso invisível. Teletransporta-se para 40 blocos acima do dono.
                this.dono = OwlEntity.this.getOwner();

                if (this.dono != null) {
                    // CÁLCULO DO REGRESSO: Mesma matemática do mergulho para o regresso ao dono
                    double anguloChegada = OwlEntity.this.random.nextDouble() * 2 * Math.PI;
                    double distanciaHorizontal = 30.0;

                    double offsetX = Math.cos(anguloChegada) * distanciaHorizontal;
                    double offsetZ = Math.sin(anguloChegada) * distanciaHorizontal;

                    OwlEntity.this.setPos(dono.getX() + offsetX, dono.getY() + 30, dono.getZ() + offsetZ);
                    fase = 6;
                } else {
                    OwlEntity.this.emMissao = false;
                    OwlEntity.this.setNoGravity(false);
                }
            }
            else if (fase == 6) {
                // NOVA PROTEÇÃO: Verifica se o dono desconectou a meio do regresso
                if (this.dono == null || this.dono.isRemoved()) {
                    OwlEntity.this.emMissao = false;
                    OwlEntity.this.setNoGravity(false); // Aterra em segurança
                    return; // Sai do tick para evitar erros
                }

                // CÁLCULO DINÂMICO PARA O REGRESSO
                double distanciaSqr = OwlEntity.this.distanceToSqr(dono);
                double velocidade = distanciaSqr > 100.0 ? 0.7 : 0.3;

                // FASE 6: Mergulho Diagonal de regresso ao dono
                double distX = dono.getX() - OwlEntity.this.getX();
                double distY = dono.getY() - OwlEntity.this.getY();
                double distZ = dono.getZ() - OwlEntity.this.getZ();

                net.minecraft.world.phys.Vec3 direcao = new net.minecraft.world.phys.Vec3(distX, distY, distZ).normalize().scale(0.3);
                OwlEntity.this.setDeltaMovement(direcao);

                // OBRIGA a coruja a olhar para o dono enquanto desce
                OwlEntity.this.getLookControl().setLookAt(dono, 30.0F, 30.0F);

                // Quando toca no dono ou no chão, a missão acaba com sucesso!
                if (OwlEntity.this.distanceTo(dono) < 2.0F || OwlEntity.this.onGround()) {
                    OwlEntity.this.emMissao = false;
                    OwlEntity.this.setNoGravity(false); // Devolve a gravidade normal ao animal
                }
            }
        }
    }
}