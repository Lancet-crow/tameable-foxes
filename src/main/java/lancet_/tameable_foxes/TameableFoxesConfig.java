package lancet_.tameable_foxes;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedList;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedFloat;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Stream;

public class TameableFoxesConfig extends Config {
    public static TameableFoxesConfig config = ConfigApiJava.registerAndLoadConfig(TameableFoxesConfig::new, RegisterType.BOTH);

    @RequiresAction(action = Action.RESTART)
    public ValidatedList<Identifier> foxTamingItems = new ValidatedList<>(List.of(
            Identifier.tryParse("glow_berries")
    ), ValidatedIdentifier.ofRegistry(Identifier.tryParse("glow_berries"), Registries.ITEM));

    @RequiresAction(action = Action.RESTART)
    public ValidatedList<Identifier> foxBreedingItems = new ValidatedList<>(List.of(
            Identifier.tryParse("sweet_berries")
    ), ValidatedIdentifier.ofRegistry(Identifier.tryParse("sweet_berries"), Registries.ITEM));

    public static List<Item> FOX_TAMING_ITEMS = List.of();
    public static List<Item> FOX_BREEDING_ITEMS = List.of();

    @RequiresAction(action = Action.RESTART)
    public boolean foxesAttackWithOwner = true;

    public boolean foxesTameDirectly = true;

    @ValidatedFloat.Restrict(min = 0f, max = 1f, type = ValidatedNumber.WidgetType.SLIDER)
    public float foxesTamingChance = 0.3f;

    public boolean foxesCanIgnoreMobGriefingRule = false;

    public boolean foxesTrustOnBorn = true;

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
        super(Identifier.of(TameableFoxes.MOD_ID, "config"));
    }

    public static void init() {
        FOX_TAMING_ITEMS = TameableFoxesConfig.config.foxTamingItems.stream().map(Registries.ITEM::get).toList();
        FOX_BREEDING_ITEMS = TameableFoxesConfig.config.foxBreedingItems.stream().map(Registries.ITEM::get).toList();
    }

    @Override
    public void onUpdateClient() {
        FOX_TAMING_ITEMS = TameableFoxesConfig.config.foxTamingItems.stream().map(Registries.ITEM::get).toList();
        FOX_BREEDING_ITEMS = TameableFoxesConfig.config.foxBreedingItems.stream().map(Registries.ITEM::get).toList();
    }
}
