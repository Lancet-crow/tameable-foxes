package lancet_.tameable_foxes;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedList;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedRegistryType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.stream.Stream;

public class TameableFoxesConfig extends Config {
    public static TameableFoxesConfig config = ConfigApiJava.registerAndLoadConfig(TameableFoxesConfig::new, RegisterType.BOTH);

    @RequiresAction(action = Action.RESTART)
    public ValidatedList<Item> foxTamingItems = new ValidatedList<>(List.of(
            Items.GLOW_BERRIES
    ), ValidatedRegistryType.of(BuiltInRegistries.ITEM));

    @RequiresAction(action = Action.RESTART)
    public ValidatedList<Item> foxBreedingItems = new ValidatedList<>(List.of(
            Items.SWEET_BERRIES
    ), ValidatedRegistryType.of(BuiltInRegistries.ITEM));

    public static List<Item> FOX_TAMING_ITEMS = List.of();
    public static List<Item> FOX_BREEDING_ITEMS = List.of();

    @RequiresAction(action = Action.RESTART)
    public boolean foxesTameDirectly = true;

    public static Stream<ItemStack> getFoxTamingItemStacks(){
        Stream<ItemStack> foxTamingItemStacks = Stream.<ItemStack>builder().build();
        for (Item item: FOX_TAMING_ITEMS
        ) {
            foxTamingItemStacks = Stream.concat(foxTamingItemStacks, Stream.of(new ItemStack(item)));
        }
        return foxTamingItemStacks;
    }

    public static Stream<ItemStack> getFoxBreedingItemStacks(){
        Stream<ItemStack> foxBreedingItemStacks = Stream.<ItemStack>builder().build();
        for (Item item: FOX_BREEDING_ITEMS
        ) {
            foxBreedingItemStacks = Stream.concat(foxBreedingItemStacks, Stream.of(new ItemStack(item)));
        }
        return foxBreedingItemStacks;
    }

    public static Stream<ItemStack> getFoxTemptingItemStacks(){
        return Stream.concat(getFoxTamingItemStacks(), getFoxBreedingItemStacks());
    }

    public TameableFoxesConfig() {
        super(ResourceLocation.fromNamespaceAndPath(TameableFoxes.MOD_ID, "config"));
    }

    public static void init() {
        FOX_TAMING_ITEMS = TameableFoxesConfig.config.foxTamingItems;
        FOX_BREEDING_ITEMS = TameableFoxesConfig.config.foxBreedingItems;
    }

    @Override
    public void onUpdateClient() {
        FOX_TAMING_ITEMS = TameableFoxesConfig.config.foxTamingItems;
        FOX_BREEDING_ITEMS = TameableFoxesConfig.config.foxBreedingItems;
    }
}
