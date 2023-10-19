package SBUtils.content;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.EnumSet;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;

import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.DrawPart.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.graphics.MultiPacker.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.power.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import java.lang.reflect.*;
import java.util.*;

import static mindustry.Vars.*;
import static mindustry.type.ItemStack.*;

import SBUtils.content.types.blocks.*;
import SBUtils.content.types.blocks.distribution.*;
import SBUtils.content.types.blocks.logic.*;
public class SBUblocks {
    public static Block

    drawProcessor;

    public static void load(){
        drawProcessor = new DrawProcessor("toggle-gate"){{
            requirements(Category.logic, with(Items.copper, 90, Items.lead, 75, Items.silicon, 65, Items.metaglass, 10));

            instructionsPerTick = 2;
        }};
    }
}
