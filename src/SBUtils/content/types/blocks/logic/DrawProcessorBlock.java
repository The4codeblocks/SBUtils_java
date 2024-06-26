package SBUtils.content.types.blocks.logic;

import arc.audio.*;
import arc.math.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.graphics.g2d.*;
import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.Bits;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;

import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ai.types.*;
import mindustry.core.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.io.TypeIO.*;
import mindustry.logic.*;
import mindustry.logic.LAssembler.*;
import mindustry.logic.LExecutor.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.meta.*;
import mindustry.world.blocks.logic.*;

import java.io.*;
import java.util.zip.*;

import static mindustry.Vars.*;

//is this too much?
import arc.*;
import mindustry.*;

public class DrawProcessorBlock extends LogicBlock {
    public static final byte
        commandClear = 0,
        commandColor = 1,
        //virtual command, unpacked in instruction
        commandColorPack = 2,
        commandStroke = 3,
        commandLine = 4,
        commandRect = 5,
        commandLineRect = 6,
        commandPoly = 7,
        commandLinePoly = 8,
        commandTriangle = 9,
        commandImage = 10,
        //note that this command actually only draws 1 character, unpacked in instruction
        commandPrint = 11,

        commandTranslate = 12,
        commandScale = 13,
        commandRotate = 14,
        commandResetTransform = 15
    ;

    public static final float scaleStep = 0.05f;

    public int maxSides = 25;

    public int displaySize = 64;
    public float scaleFactor = 1f;

    public DrawProcessorBlock(String name){
        super(name);
        update = true;
        solid = true;
        canOverdrive = false;
        group = BlockGroup.logic;
        drawDisabled = false;
        envEnabled = Env.any;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.displaySize, "@x@", displaySize, displaySize);
    }

    public static class DrawProcessorBuild extends LogicBuild{
        public FrameBuffer buffer;
        public float color = Color.whiteFloatBits;
        public float stroke = 1f;
        public LongQueue commands = new LongQueue(256);
        public @Nullable Mat transform;

        @Override
        public void draw(){
            super.draw();

            //don't even bother processing anything when displays are off.
            if(!Vars.renderer.drawDisplays) return;

            Draw.draw(Draw.z(), () -> {
                if(buffer == null){
                    buffer = new FrameBuffer(displaySize, displaySize);
                    //clear the buffer - some OSs leave garbage in it
                    buffer.begin(Pal.darkerMetal);
                    buffer.end();
                }
            });

            //don't bother processing commands if displays are off
            if(!commands.isEmpty()){
                Draw.draw(Draw.z(), () -> {
                    Tmp.m1.set(Draw.proj());
                    Tmp.m2.set(Draw.trans());
                    Draw.proj(0, 0, displaySize, displaySize);
                    if(transform != null){
                        Draw.trans(transform);
                    }
                    buffer.begin();
                    Draw.color(color);
                    Lines.stroke(stroke);

                    while(!commands.isEmpty()){
                        long c = commands.removeFirst();
                        int type = DisplayCmd.type(c);
                        int x = unpackSign(DisplayCmd.x(c)), y = unpackSign(DisplayCmd.y(c)),
                        p1 = unpackSign(DisplayCmd.p1(c)), p2 = unpackSign(DisplayCmd.p2(c)), p3 = unpackSign(DisplayCmd.p3(c)), p4 = unpackSign(DisplayCmd.p4(c));

                        switch(type){
                            case commandClear -> {
                                //discard any pending batched sprites, so they don't get drawn over the cleared screen later
                                //Draw.discard(); faulty
                                Core.graphics.clear(x / 255f, y / 255f, p1 / 255f, 1f);
                            }
                            case commandLine -> Lines.line(x, y, p1, p2);
                            case commandRect -> Fill.crect(x, y, p1, p2);
                            case commandLineRect -> Lines.rect(x, y, p1, p2);
                            case commandPoly -> Fill.poly(x, y, Math.min(p1, maxSides), p2, p3);
                            case commandLinePoly -> Lines.poly(x, y, Math.min(p1, maxSides), p2, p3);
                            case commandTriangle -> Fill.tri(x, y, p1, p2, p3, p4);
                            case commandColor -> Draw.color(this.color = Color.toFloatBits(x, y, p1, p2));
                            case commandStroke -> Lines.stroke(this.stroke = x);
                            case commandImage -> {
                                var icon = Fonts.logicIcon(p1);
                                Draw.rect(Fonts.logicIcon(p1), x, y, p2, p2 / icon.ratio(), p3);
                            }
                            case commandTranslate -> Draw.trans((transform == null ? (transform = new Mat()) : transform).translate(x, y));
                            case commandScale -> Draw.trans((transform == null ? (transform = new Mat()) : transform).scale(x * scaleStep, y * scaleStep));
                            case commandRotate-> Draw.trans((transform == null ? (transform = new Mat()) : transform).rotate(p1));
                            case commandResetTransform -> Draw.trans((transform == null ? (transform = new Mat()) : transform).idt());
                        }
                    }

                    buffer.end();
                    Draw.proj(Tmp.m1);
                    Draw.trans(Tmp.m2);
                    Draw.reset();
                });
            }

            Draw.blend(Blending.disabled);
            Draw.draw(Draw.z(), () -> {
                if(buffer != null){
                    Draw.rect(Draw.wrap(buffer.getTexture()), x, y, buffer.getWidth() * scaleFactor * Draw.scl, -buffer.getHeight() * scaleFactor * Draw.scl);
                }
            });
            Draw.blend();
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            if(transform != null){
                write.bool(true);
                for(int i = 0; i < transform.val.length; i++){
                    write.f(transform.val[i]);
                }
            }else{
                write.bool(false);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                boolean hasTransform = read.bool();
                if(hasTransform){
                    transform = new Mat();

                    for(int i = 0; i < transform.val.length; i++){
                        transform.val[i] = read.f();
                    }
                }
            }
        }

        @Override
        public void remove(){
            super.remove();
            if(buffer != null){
                buffer.dispose();
                buffer = null;
            }
        }
    }

    static int unpackSign(int value){
        return (value & 0b0111111111) * ((value & (0b1000000000)) != 0 ? -1 : 1);
    }

    public enum GraphicsType{
        clear,
        color,
        //virtual
        col,
        stroke,
        line,
        rect,
        lineRect,
        poly,
        linePoly,
        triangle,
        image,
        //note that this command actually only draws 1 character, unpacked in instruction
        print,
        translate,
        scale,
        rotate,
        reset
        ;

        public static final GraphicsType[] all = values();
    }

    static class DisplayCmdStruct{
        public int type;

        //at least 9 bits are required for full 360 degrees
        public int x, y, p1, p2, p3, p4;
    }
}
