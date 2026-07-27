/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Chud Pixel Dungeon
 * Copyright (C) 2026 Trashbox Bobylev
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StatusPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class WndLevelUp extends Window {
    private static final int WIDTH		= 130;
    private static final float GAP		= 2;

    public WndLevelUp() {
        super();

        IconTitle titlebar = new IconTitle();
        titlebar.icon(Icons.get(Icons.CHUD_LEVEL) );
        titlebar.label(Messages.get(this, "title"));
        titlebar.setRect( 0, 0, WIDTH-16, 0 );
        add( titlebar );

        IconButton random = new IconButton(Icons.SHUFFLE.get()){
            @Override
            protected void onClick() {
                super.onClick();
                GameScene.show(new WndOptions(Icons.SHUFFLE.get(),
                        Messages.get(WndLevelUp.class, "random_title"),
                        Messages.get(WndLevelUp.class, "random_sure"),
                        Messages.get(WndLevelUp.class, "yes"),
                        Messages.get(WndLevelUp.class, "no")){
                    @Override
                    protected void onSelect(int index) {
                        super.onSelect(index);
                        if (index == 0){
                            WndLevelUp.this.hide();
                            Benefits benefit = Random.oneOf(Benefits.values());
                            benefit.onSelection();
                            GLog.p( Messages.get(WndLevelUp.class,  benefit.name() + ".upgrade") );
                        }
                    }
                });
            }

            @Override
            public void update() {
                if (Statistics.qualifiedForRandomVictoryBadge){
                    icon.tint(1, 1, 1, (float)Math.abs(Math.cos(1.5f*Math.PI* Game.timeTotal)/2f));
                }
                super.update();
            }

            @Override
            protected String hoverText() {
                return Messages.get(WndLevelUp.class, "random_title");
            }
        };
        random.setRect(WIDTH-16, 0, 16, 16);
        add(random);

        RenderedTextBlock message = PixelScene.renderTextBlock( 6 );
        message.text( Messages.get(this, "message"), WIDTH );
        message.setPos( titlebar.left(), titlebar.bottom() + GAP );
        add( message );

        float pos = message.bottom() + 3*GAP;

        for (Benefits benefit : Benefits.values()){
            RedButton btnCls = new RedButton( Messages.get(this, benefit.name() + ".desc"), 6 ) {
                @Override
                protected void onClick() {
                    GameScene.show(new WndOptions(benefit.icon(),
                            Messages.titleCase(Messages.get(WndLevelUp.class, benefit.name() + ".title")),
                            Messages.get(WndLevelUp.this, "are_you_sure"),
                            Messages.get(WndLevelUp.this, "yes"),
                            Messages.get(WndLevelUp.this, "no")){
                        @Override
                        protected void onSelect(int index) {
                            hide();
                            if (index == 0 && WndLevelUp.this.parent != null){
                                WndLevelUp.this.hide();
                                benefit.onSelection();
                                GLog.p( Messages.get(WndLevelUp.class,  benefit.name() + ".upgrade") );
                                Statistics.qualifiedForRandomVictoryBadge = false;
                            }
                        }
                    });
                }
            };
            btnCls.icon(benefit.icon());
            btnCls.leftJustify = true;
            btnCls.multiline = true;
            btnCls.setSize(WIDTH, btnCls.reqHeight()+2);
            btnCls.setRect( 0, pos, WIDTH, btnCls.reqHeight()+2);
            add( btnCls );

            pos = btnCls.bottom() + GAP;
        }

        resize( WIDTH, (int) pos);
    }

    @Override
    public void onBackPressed() {}

    public enum Benefits {
        HP{
            @Override
            public void onSelection() {
                Dungeon.hero.hp_lvl++;
                Dungeon.hero.updateHT(true);
            }

            @Override
            public Image icon() {
                return new TalentIcon(Talent.IRON_WILL);
            }
        },
        POW{
            @Override
            public void onSelection() {
                Dungeon.hero.STR++;
            }

            @Override
            public Image icon() {
                return new TalentIcon(Talent.STRONGMAN);
            }
        },
        WIS{
            @Override
            public void onSelection() {
                for (int i = 1; i <= 4; i++){
                    if (Dungeon.hero.lvl < Talent.tierLevelThresholds[i+1]){
                        Dungeon.hero.tal_lvl[i-1] += 1;
                    }
                }
                Sample.INSTANCE.playDelayed(Assets.Sounds.LEVELUP, 0.3f, 0.7f, 1.2f);
                Sample.INSTANCE.playDelayed(Assets.Sounds.LEVELUP, 0.6f, 0.7f, 1.2f);
                new Flare( 6, 32 ).color(0xFFFF00, true).show( Dungeon.hero.sprite, 2f );
                StatusPane.talentBlink = 10f;
                WndHero.lastIdx = 1;
            }

            @Override
            public Image icon() {
                return Icons.get(Icons.TALENT);
            }
        },
        CHA{
            @Override
            public void onSelection() {
                Dungeon.hero.rng_lvl++;
                Dungeon.hero.updateHT(false);
            }

            @Override
            public Image icon() {
                return new TalentIcon(Talent.ENHANCED_RINGS);
            }
        },;

        public abstract Image icon();

        public abstract void onSelection();
    }
}
