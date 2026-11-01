package de.nevergate.verkehr;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;

@Mod(NgTraffic.ID)
public final class NgTraffic {
    public static final String ID="ng_verkehr";
    public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,ID);
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,ID);
    public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,ID);
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,ID);
    public static final RegistryObject<MenuType<ControllerMenu>> CONTROLLER_MENU=MENUS.register("controller",()->IForgeMenuType.create(ControllerMenu::new));
    public static final RegistryObject<MenuType<WayfindingMenu>> WAYFINDING_MENU=MENUS.register("wayfinding",()->IForgeMenuType.create(WayfindingMenu::new));
    public static final RegistryObject<MenuType<RoadSystemMenu>> ROAD_SYSTEM_MENU=MENUS.register("road_system",()->IForgeMenuType.create(RoadSystemMenu::new));
    public static final RegistryObject<MenuType<ConstructionMenu>> CONSTRUCTION_MENU=MENUS.register("construction",()->IForgeMenuType.create(ConstructionMenu::new));
    public static final RegistryObject<MenuType<TrafficNodeMenu>> TRAFFIC_NODE_MENU=MENUS.register("traffic_node",()->IForgeMenuType.create(TrafficNodeMenu::new));
    public static final Map<String,RegistryObject<Block>> CONTENT=new LinkedHashMap<>();
    public static BlockBehaviour.Properties metal() { return BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.METAL); }
    public static RegistryObject<Block> add(String id,Supplier<Block> factory) {
        return add(id,factory,block->block instanceof MountedBlock?new MountingBlockItem(block):new BlockItem(block,new Item.Properties()));
    }
    public static RegistryObject<Block> add(String id,Supplier<Block> factory,Function<Block,Item> itemFactory) {
        RegistryObject<Block> block=BLOCKS.register(id,factory);
        CONTENT.put(id,block);
        ITEMS.register(id,()->itemFactory.apply(block.get()));
        return block;
    }
    public static final RegistryObject<Block> ASPHALT=add("asphalt",()->new Block(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.STONE)));
    public static final Map<String,RegistryObject<Item>> LANE_MODULES=new LinkedHashMap<>();
    static {
        LANE_MODULES.put("asphalt_lane_4",ITEMS.register("asphalt_lane_4",()->new LaneModuleItem(ASPHALT,1)));
        for(String direction:new String[]{"straight","left","right"}) {
            String id="lane_arrow_"+direction;
            var block=BLOCKS.register(id,LaneArrowBlock::new);
            LANE_MODULES.put(id,ITEMS.register(id,()->new LaneModuleItem(block,4)));
        }
    }
    static {
        for(String id:new String[]{"line_white","line_white_dashed","line_white_double","line_yellow","line_yellow_dashed","stop_line","zebra","arrow_straight","arrow_left","arrow_right","arrow_straight_left","arrow_straight_right"})
            add(id,()->new OrientedBlock(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.STONE),false));
        for(String id:new String[]{"line_white_edge","line_white_short","line_white_solid_dashed","guide_left","guide_right","hatched_area","bus_lane","bicycle_lane","yield_line","arrow_left_right","arrow_all","chevron_left","chevron_right","parking_bay"})add(id,()->new OrientedBlock(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.STONE),false));
        for(String id:new String[]{"island_middle","island_edge","island_corner","island_end","curb_ramp","curb_low","guardrail_end","guardrail_transition","signal_mast","signal_mast_top","signal_boom","signal_boom_end","signal_bracket","sign_bracket"})add(id,()->new RoadPartBlock(id));
        add("pavement",()->new Block(BlockBehaviour.Properties.of().strength(2f)));
        add("curb",()->new SlabBlock(BlockBehaviour.Properties.of().strength(2f)));
        add("guardrail",GuardrailBlock::new);
        add("post",()->new FenceBlock(metal()));
        for(String id:new String[]{"blank_sign","sign_101","sign_205","sign_206","sign_250","sign_267","sign_274_30","sign_274_50","sign_274_80","sign_274_100","sign_274_120","sign_306","sign_314","sign_1004_30"})
            add(id,()->new MountedBlock(metal().noOcclusion(),true));
    }
    static {
        for(String id:new String[]{"work_bollard_left","work_bollard_right","work_barrier","work_fence","work_base"})add(id,()->new RoadPartBlock(id));
        for(String id:new String[]{"work_yellow_double","work_yellow_short","work_yellow_arrow_straight","work_yellow_arrow_left","work_yellow_arrow_right","work_yellow_arrow_straight_left","work_yellow_arrow_straight_right","work_yellow_hatched","work_yellow_guide_left","work_yellow_guide_right"})add(id,()->new OrientedBlock(BlockBehaviour.Properties.of().strength(2.5f).sound(SoundType.STONE),false));
        for(String id:new String[]{"work_sign_construction","work_sign_narrow","work_sign_pass_left","work_sign_pass_right","work_sign_end"})add(id,()->new MountedBlock(metal().noOcclusion(),true));
    }
    static {
        for(String id:SignExpansion.IDS) add(id,()->new MountedBlock(metal().noOcclusion(),true));
    }
    public static final java.util.Map<TrafficNodeBlock.Kind,RegistryObject<Block>> TRAFFIC_NODES=new java.util.EnumMap<>(TrafficNodeBlock.Kind.class);
    static {for(var kind:TrafficNodeBlock.Kind.values())TRAFFIC_NODES.put(kind,add("traffic_"+kind.name().toLowerCase(java.util.Locale.ROOT),()->new TrafficNodeBlock(kind)));}
    public static final RegistryObject<Item> TRAFFIC_LINKER=ITEMS.register("traffic_linker",TrafficNodeLinker::new);
    public static final RegistryObject<BlockEntityType<TrafficNodeEntity>> TRAFFIC_NODE_ENTITY=ENTITIES.register("traffic_node",()->BlockEntityType.Builder.of(TrafficNodeEntity::new,TRAFFIC_NODES.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final java.util.Map<ServiceBlock.Kind,RegistryObject<Block>> SERVICES=new java.util.EnumMap<>(ServiceBlock.Kind.class);
    static {for(var kind:ServiceBlock.Kind.values())SERVICES.put(kind,add("service_"+kind.name().toLowerCase(java.util.Locale.ROOT),()->new ServiceBlock(kind)));}
    public static final RegistryObject<Item> SERVICE_LINKER=ITEMS.register("service_linker",ServiceLinker::new);
    public static final RegistryObject<Item> PARK_TICKET=ITEMS.register("park_ticket",()->new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PARK_PASS=ITEMS.register("park_pass",()->new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockEntityType<ServiceEntity>> SERVICE_ENTITY=ENTITIES.register("service",()->BlockEntityType.Builder.of(ServiceEntity::new,SERVICES.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<MenuType<ServiceMenu>> SERVICE_MENU=MENUS.register("service",()->IForgeMenuType.create(ServiceMenu::new));
    public static final java.util.Map<OperationsBlock.Kind,RegistryObject<Block>> OPERATIONS=new java.util.EnumMap<>(OperationsBlock.Kind.class);
    static {for(var kind:OperationsBlock.Kind.values())OPERATIONS.put(kind,add("ops_"+kind.name().toLowerCase(java.util.Locale.ROOT),()->new OperationsBlock(kind)));}
    public static final RegistryObject<Item> OPERATIONS_LINKER=ITEMS.register("operations_linker",OperationsLinker::new);
    public static final RegistryObject<Item> ACCESS_CARD=ITEMS.register("access_card",()->new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<BlockEntityType<OperationsEntity>> OPERATIONS_ENTITY=ENTITIES.register("operations",()->BlockEntityType.Builder.of(OperationsEntity::new,OPERATIONS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build(null));
    public static final RegistryObject<MenuType<OperationsMenu>> OPERATIONS_MENU=MENUS.register("operations",()->IForgeMenuType.create(OperationsMenu::new));
    public static final RegistryObject<Block> WORK_TRAILER=add("work_warning_trailer",()->new ConstructionBlock(ConstructionBlock.Kind.TRAILER));
    public static final RegistryObject<Block> WORK_RUNNING_LAMP=add("work_running_lamp",()->new ConstructionBlock(ConstructionBlock.Kind.LAMP));
    public static final RegistryObject<Block> WORK_TIMED=add("work_timed_barrier",()->new ConstructionBlock(ConstructionBlock.Kind.TIMED));
    public static final RegistryObject<Block> WORK_GATE=add("work_extending_gate",()->new ConstructionBlock(ConstructionBlock.Kind.GATE));
    public static final RegistryObject<Block> WORK_CABLE=add("work_cable_bridge",()->new ConstructionBlock(ConstructionBlock.Kind.CABLE));
    public static final RegistryObject<Block> WORK_HOSE=add("work_hose_bridge",()->new ConstructionBlock(ConstructionBlock.Kind.HOSE));
    public static final RegistryObject<Block> WORK_BRIDGE=add("work_footbridge",()->new ConstructionBlock(ConstructionBlock.Kind.BRIDGE));
    public static final RegistryObject<Block> WORK_MAST=add("work_light_mast",()->new ConstructionBlock(ConstructionBlock.Kind.MAST));
    public static final RegistryObject<Block> WORK_ACCESS=add("work_access_gate",()->new ConstructionBlock(ConstructionBlock.Kind.ACCESS));
    public static final RegistryObject<Block> WORK_EXTENSION=BLOCKS.register("work_gate_extension",()->new ConstructionBlock(ConstructionBlock.Kind.EXTENSION));
    public static final RegistryObject<Item> WORK_LINKER=ITEMS.register("work_linker",ConstructionLinker::new);
    public static final RegistryObject<BlockEntityType<ConstructionEntity>> CONSTRUCTION_ENTITY=ENTITIES.register("construction",()->BlockEntityType.Builder.of(ConstructionEntity::new,WORK_TRAILER.get(),WORK_RUNNING_LAMP.get(),WORK_TIMED.get(),WORK_GATE.get(),WORK_CABLE.get(),WORK_HOSE.get(),WORK_BRIDGE.get(),WORK_MAST.get(),WORK_ACCESS.get(),WORK_EXTENSION.get()).build(null));
    public static final RegistryObject<Block> WORK_LAMP=add("work_lamp",()->new WorksiteBlock(false));
    public static final RegistryObject<Block> WORK_ARROW=add("work_arrow_board",()->new WorksiteBlock(true));
    public static final RegistryObject<Block> MOBILE_SIGNAL=add("mobile_signal",MobileSignalBlock::new);
    public static final RegistryObject<Block> SIGNAL=add("signal",SignalBlock::new);
    public static final RegistryObject<Block> SIGNAL_LEFT=add("signal_left",()->new SignalBlock(SignalKind.LEFT));
    public static final RegistryObject<Block> SIGNAL_RIGHT=add("signal_right",()->new SignalBlock(SignalKind.RIGHT));
    public static final RegistryObject<Block> SIGNAL_STRAIGHT=add("signal_straight",()->new SignalBlock(SignalKind.STRAIGHT));
    public static final RegistryObject<Block> SIGNAL_STRAIGHT_LEFT=add("signal_straight_left",()->new SignalBlock(SignalKind.STRAIGHT_LEFT));
    public static final RegistryObject<Block> SIGNAL_STRAIGHT_RIGHT=add("signal_straight_right",()->new SignalBlock(SignalKind.STRAIGHT_RIGHT));
    public static final RegistryObject<Block> SIGNAL_LEFT_RIGHT=add("signal_left_right",()->new SignalBlock(SignalKind.LEFT_RIGHT));
    public static final RegistryObject<Block> SIGNAL_ALL=add("signal_all",()->new SignalBlock(SignalKind.ALL));
    public static final RegistryObject<Block> BICYCLE_SIGNAL=add("bicycle_signal",()->new SignalBlock(SignalKind.BICYCLE));
    public static final RegistryObject<Block> WARNING_SIGNAL=add("warning_signal",()->new SignalBlock(SignalKind.WARNING));
    public static final RegistryObject<Block> CONTROLLER=add("controller",()->new DeviceBlock(DeviceBlock.Kind.CONTROLLER));
    public static final RegistryObject<Block> DISPLAY=add("digital_display",()->new DeviceBlock(DeviceBlock.Kind.DISPLAY));
    public static final RegistryObject<Block> PARKING=add("parking_display",()->new DeviceBlock(DeviceBlock.Kind.PARKING));
    public static final RegistryObject<Block> PEDESTRIAN_SIGNAL=add("pedestrian_signal",()->new PedestrianBlock(false));
    public static final RegistryObject<Block> PEDESTRIAN_BUTTON=add("pedestrian_button",()->new PedestrianBlock(true),PedestrianButtonItem::new);
    public static final RegistryObject<Item> SIGNAL_LINKER=ITEMS.register("signal_linker",SignalLinkerItem::new);
    public static final RegistryObject<Block> TOWN_SIGN=add("town_sign",()->new WayfindingBlock(WayfindingBlock.Kind.TOWN));
    public static final RegistryObject<Block> TOWN_EXIT_SIGN=add("town_exit_sign",()->new WayfindingBlock(WayfindingBlock.Kind.TOWN_EXIT));
    public static final RegistryObject<Block> ROAD_GUIDE=add("road_guide",()->new WayfindingBlock(WayfindingBlock.Kind.ROAD));
    public static final RegistryObject<Block> MOTORWAY_GUIDE=add("motorway_guide",()->new WayfindingBlock(WayfindingBlock.Kind.MOTORWAY));
    public static final RegistryObject<Block> EXIT_GUIDE=add("exit_guide",()->new WayfindingBlock(WayfindingBlock.Kind.EXIT));
    public static final RegistryObject<Block> DISTANCE_GUIDE=add("distance_guide",()->new WayfindingBlock(WayfindingBlock.Kind.DISTANCE));
    public static final RegistryObject<Block> DETOUR_GUIDE=add("detour_guide",()->new WayfindingBlock(WayfindingBlock.Kind.DETOUR));
    static {for(String id:new String[]{"bridge_support","bridge_crossbeam","bridge_hanger"})add(id,()->new RoadPartBlock(id));}
    public static final RegistryObject<Block> ROAD_CONTROLLER=add("road_controller",()->new RoadSystemBlock(RoadSystemBlock.Role.CONTROLLER));
    public static final RegistryObject<Block> PARKING_AGGREGATOR=add("parking_aggregator",()->new RoadSystemBlock(RoadSystemBlock.Role.AGGREGATOR));
    public static final RegistryObject<Block> VARIABLE_SIGN=add("variable_sign",()->new RoadSystemBlock(RoadSystemBlock.Role.TRAFFIC_SIGN));
    public static final RegistryObject<Block> LANE_BOARD=add("lane_board",()->new RoadSystemBlock(RoadSystemBlock.Role.LANE_SIGN));
    public static final RegistryObject<Block> PARKING_GUIDE=add("parking_guide",()->new RoadSystemBlock(RoadSystemBlock.Role.PARKING_GUIDE));
    public static final RegistryObject<Block> PARKING_BOARD=add("parking_board",()->new RoadSystemBlock(RoadSystemBlock.Role.PARKING_BOARD));
    public static final RegistryObject<Block> ENTRY_SENSOR=add("entry_sensor",()->new RoadSystemBlock(RoadSystemBlock.Role.ENTRY_SENSOR));
    public static final RegistryObject<Block> EXIT_SENSOR=add("exit_sensor",()->new RoadSystemBlock(RoadSystemBlock.Role.EXIT_SENSOR));
    public static final RegistryObject<Block> SPACE_SENSOR=add("parking_space_sensor",()->new RoadSystemBlock(RoadSystemBlock.Role.SPACE_SENSOR));
    public static final RegistryObject<Block> PARKING_BARRIER=add("parking_barrier",()->new RoadSystemBlock(RoadSystemBlock.Role.BARRIER));
    public static final RegistryObject<Block> HEIGHT_SIGN=add("parking_height_sign",()->new RoadSystemBlock(RoadSystemBlock.Role.HEIGHT_SIGN));
    public static final RegistryObject<Item> ROAD_LINKER=ITEMS.register("road_linker",RoadSystemLinker::new);
    public static final RegistryObject<BlockEntityType<RoadSystemEntity>> ROAD_SYSTEM_ENTITY=ENTITIES.register("road_system",()->BlockEntityType.Builder.of(RoadSystemEntity::new,ROAD_CONTROLLER.get(),PARKING_AGGREGATOR.get(),VARIABLE_SIGN.get(),LANE_BOARD.get(),PARKING_GUIDE.get(),PARKING_BOARD.get(),ENTRY_SENSOR.get(),EXIT_SENSOR.get(),SPACE_SENSOR.get(),PARKING_BARRIER.get(),HEIGHT_SIGN.get()).build(null));
    public static final RegistryObject<BlockEntityType<WayfindingEntity>> WAYFINDING_ENTITY=ENTITIES.register("wayfinding",()->BlockEntityType.Builder.of(WayfindingEntity::new,TOWN_SIGN.get(),TOWN_EXIT_SIGN.get(),ROAD_GUIDE.get(),MOTORWAY_GUIDE.get(),EXIT_GUIDE.get(),DISTANCE_GUIDE.get(),DETOUR_GUIDE.get()).build(null));
    public static final RegistryObject<BlockEntityType<DeviceEntity>> DEVICE=ENTITIES.register("device",()->BlockEntityType.Builder.of(DeviceEntity::new,CONTROLLER.get(),DISPLAY.get(),PARKING.get()).build(null));
    public static final RegistryObject<BlockEntityType<SignalEntity>> SIGNAL_ENTITY=ENTITIES.register("signal",()->BlockEntityType.Builder.of(SignalEntity::new,CONTENT.values().stream().map(RegistryObject::get).filter(b->b instanceof SignalBlock).toArray(Block[]::new)).build(null));
    public static final RegistryObject<BlockEntityType<PedestrianEntity>> PEDESTRIAN_ENTITY=ENTITIES.register("pedestrian",()->BlockEntityType.Builder.of(PedestrianEntity::new,PEDESTRIAN_SIGNAL.get(),PEDESTRIAN_BUTTON.get()).build(null));
    public static Item categoryItem(String id) {
        if(LANE_MODULES.containsKey(id)) return LANE_MODULES.get(id).get();
        return id.equals("operations_linker")?OPERATIONS_LINKER.get():id.equals("access_card")?ACCESS_CARD.get():id.equals("service_linker")?SERVICE_LINKER.get():id.equals("park_ticket")?PARK_TICKET.get():id.equals("park_pass")?PARK_PASS.get():id.equals("traffic_linker")?TRAFFIC_LINKER.get():id.equals("work_linker")?WORK_LINKER.get():id.equals("signal_linker")?SIGNAL_LINKER.get():id.equals("road_linker")?ROAD_LINKER.get():CONTENT.get(id).get().asItem();
    }
    static {
        var expected=new java.util.HashSet<>(CONTENT.keySet());expected.add("signal_linker");expected.add("road_linker");expected.add("work_linker");expected.add("traffic_linker");
        expected.add("operations_linker");expected.add("access_card");expected.add("service_linker");expected.add("park_ticket");expected.add("park_pass");expected.addAll(LANE_MODULES.keySet());CreativeCategories.validate(expected);
        var categories=CreativeCategories.entries();
        for(int i=0;i<categories.size();i++){
            var category=categories.get(i);String previous=i==0?null:categories.get(i-1).id();
            TABS.register(category.id(),()->{
                var builder=CreativeModeTab.builder().title(Component.translatable("itemGroup.ng_verkehr."+category.id()))
                    .icon(()->new ItemStack(categoryItem(category.icon())))
                    .displayItems((parameters,output)->category.items().forEach(id->output.accept(categoryItem(id))));
                if(previous!=null)builder.withTabsAfter(new net.minecraft.resources.ResourceLocation(ID,previous));
                return builder.build();
            });
        }
    }
    public NgTraffic() {
        NgVehiclesCompat.register();WayfindingNetwork.register();
        IEventBus bus=FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus); ITEMS.register(bus); ENTITIES.register(bus); TABS.register(bus); MENUS.register(bus);
        MinecraftForge.EVENT_BUS.addListener(OperationsCommands::register);MinecraftForge.EVENT_BUS.addListener(ServiceCommands::register);MinecraftForge.EVENT_BUS.addListener(TrafficCommands::register);MinecraftForge.EVENT_BUS.addListener(ConstructionCommands::register);MinecraftForge.EVENT_BUS.addListener(TrafficNodeCommands::register);
    }
}

