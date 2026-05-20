package lancet_.tameable_foxes;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedList;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Stream;

public class TameableFoxesConfig extends Config {
    public static TameableFoxesConfig config = ConfigApiJava.registerAndLoadConfig(TameableFoxesConfig::new, RegisterType.BOTH);
    public static List<Item> FOX_TAMING_ITEMS = List.of();
    public static List<Item> FOX_BREEDING_ITEMS = List.of();
    public static List<Item> ITEMS_RESTRICTED_TO_PICK = List.of();
    public ValidatedList<ResourceLocation> foxTamingItems = new ValidatedList<>(List.of(
            ResourceLocation.tryParse("glow_berries")
    ), ValidatedIdentifier.ofRegistry(ResourceLocation.tryParse("glow_berries"), BuiltInRegistries.ITEM));
    public ValidatedList<ResourceLocation> foxBreedingItems = new ValidatedList<>(List.of(
            ResourceLocation.tryParse("sweet_berries")
    ), ValidatedIdentifier.ofRegistry(ResourceLocation.tryParse("sweet_berries"), BuiltInRegistries.ITEM));
    public ValidatedList<ResourceLocation> itemsRestrictedToPick = new ValidatedList<>(List.of(), ValidatedIdentifier.ofRegistry(ResourceLocation.tryParse(""), BuiltInRegistries.ITEM));
    public boolean foxesAttackWithOwner = false;

    public boolean foxesTameDirectly = true;

    @ValidatedFloat.Restrict(min = 0f, max = 1f, type = ValidatedNumber.WidgetType.SLIDER)
    public float foxesTamingChance = 0.3f;

    public boolean foxesCanIgnoreMobGriefingRule = false;

    public boolean foxesTrustOnBorn = true;

    public boolean untamedFoxesCanBeTempted = false;

    public boolean untamedWolvesAttackTamedFoxes = true;

    public TameableFoxesConfig() {
        super(ResourceLocation.tryBuild(TameableFoxes.MOD_ID, "config"));
    }

    public static Stream<ItemStack> getFoxTamingItemStacks() {
        Stream<ItemStack> foxTamingItemStacks = Stream.<ItemStack>builder().build();
        for (Item item : FOX_TAMING_ITEMS
        ) {
            foxTamingItemStacks = Stream.concat(foxTamingItemStacks, Stream.of(new ItemStack(item)));
        }
        return foxTamingItemStacks;
    }

    public static Stream<ItemStack> getFoxBreedingItemStacks() {
        Stream<ItemStack> foxBreedingItemStacks = Stream.<ItemStack>builder().build();
        for (Item item : FOX_BREEDING_ITEMS
        ) {
            foxBreedingItemStacks = Stream.concat(foxBreedingItemStacks, Stream.of(new ItemStack(item)));
        }
        return foxBreedingItemStacks;
    }

    public static Stream<ItemStack> getFoxTemptingItemStacks() {
        return Stream.concat(getFoxTamingItemStacks(), getFoxBreedingItemStacks());
    }

    public static void init() {
        FOX_TAMING_ITEMS = TameableFoxesConfig.config.foxTamingItems.stream().map(BuiltInRegistries.ITEM::get).toList();
        FOX_BREEDING_ITEMS = TameableFoxesConfig.config.foxBreedingItems.stream().map(BuiltInRegistries.ITEM::get).toList();
        ITEMS_RESTRICTED_TO_PICK = TameableFoxesConfig.config.itemsRestrictedToPick.stream().map(BuiltInRegistries.ITEM::get).toList();
    }

    @Override
    public void onUpdateClient() {
        FOX_TAMING_ITEMS = TameableFoxesConfig.config.foxTamingItems.stream().map(BuiltInRegistries.ITEM::get).toList();
        FOX_BREEDING_ITEMS = TameableFoxesConfig.config.foxBreedingItems.stream().map(BuiltInRegistries.ITEM::get).toList();
        ITEMS_RESTRICTED_TO_PICK = TameableFoxesConfig.config.itemsRestrictedToPick.stream().map(BuiltInRegistries.ITEM::get).toList();
    }
}