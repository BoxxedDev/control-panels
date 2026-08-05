package moth.boxxed.panels.api.wiki;

import moth.boxxed.panels.Dashpanels;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import oshi.util.tuples.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikiScreen extends Screen {
    private static final ResourceLocation EXTRA_SPRITES = Dashpanels.path("textures/gui/wiki/sprites.png");

    private final Map<WikiCategory, SidebarDropDown> categoryDropdowns = new HashMap<>();
    private final List<SidebarButton> miscCategoryButtons = new ArrayList<>();
    private final List<ReferenceButton> references = new ArrayList<>();

    private WikiPage currentPage;

    private static final int SIDEBAR_WIDTH = 96;

    protected WikiScreen() {
        super(Component.translatable("dashpanels.wiki.title"));

        for (WikiCategory category : WikiableEntries.getAllCategories()) {
            if (category.isMisc()) {
                List<WikiPage> pages = WikiableEntries.getPagesInCategory(category);
                for (WikiPage page : pages) {
                    this.miscCategoryButtons.add(
                            this.addWidget(new SidebarButton(page))
                    );
                }
                continue;
            }

            List<WikiPage> pages = WikiableEntries.getPagesInCategory(category);
            if (pages.isEmpty())
                continue;

            categoryDropdowns.put(
                    category,
                    this.addWidget(
                            new SidebarDropDown(pages, category.getComponent())
                    )
            );
        }
    }

    public void setPage(WikiPage page) {
        if (page == this.currentPage) {
            this.currentPage = null;
            this.reconstructReferences();
            return;
        }

        SidebarDropDown dropDown = this.categoryDropdowns.get(page.getCategory());
        if (dropDown != null) {
            dropDown.setToggled(true);
        }

        this.currentPage = page;
        this.reconstructReferences();
    }

    private void reconstructReferences() {
        this.references.forEach(this::removeWidget);
        this.references.clear();

        if (currentPage == null)
            return;

        for (int i = 0; i < this.currentPage.getParagraphs(); i++) {
            String paragraphUnformatted = this.currentPage.getParagraph(i).getString();

            for (String sub : paragraphUnformatted.split(" ")) {
                String[] candidate = sub.split(":");

                if (candidate.length == 2) {
                    ResourceLocation location = ResourceLocation.parse(sub);
                    WikiPage page = WikiableEntries.pageFor(location);
                    if (WikiableEntries.exists(location) && page != null) {
                        Component pageTitle = page.getTitle();
                        int width = this.font.width(pageTitle);

                        this.references.add(
                                this.addWidget(new ReferenceButton(page, width, pageTitle))
                        );
                    }
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBlurredBackground(partialTick);

        //Render the gray background stuff
        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 0xC8404040);

        this.renderSidebar(guiGraphics, mouseX, mouseY, partialTick);
        this.renderWikiPage(guiGraphics, mouseX, mouseY, partialTick);
    }

    public void renderSidebar(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        List<AbstractWidget> allSideWidgets = new ArrayList<>();

        for (SidebarDropDown dropDown : this.categoryDropdowns.values()) {
            allSideWidgets.add(dropDown);
            dropDown.addSubButtons(allSideWidgets);
        }
        allSideWidgets.addAll(this.miscCategoryButtons);

        for (int i = 0; i < allSideWidgets.size(); i++) {
            AbstractWidget widget = allSideWidgets.get(i);
            if (i%2==0) {
                guiGraphics.fill(0, i*16, SIDEBAR_WIDTH, i*16 + 16, 0x18b5b5b5);
            }

            widget.setX(0);
            widget.setY(i*16);
            widget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.fill(SIDEBAR_WIDTH, 0, SIDEBAR_WIDTH +2, guiGraphics.guiHeight(), 0xC8262626);
    }

    public void renderWikiPage(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = SIDEBAR_WIDTH+4;

        if (currentPage == null) {
            int centerX = (guiGraphics.guiWidth()+left)/2;
            int centerY = (guiGraphics.guiHeight())/2;

            guiGraphics.drawCenteredString(this.font, Component.translatable("dashpanels.wiki.select_page"), centerX, centerY+this.font.lineHeight/2, 0xFFFFFF);

            return;
        }

        int pageWidth = guiGraphics.guiWidth()-left-4;

        Component title = this.currentPage.getTitle().copy().withStyle(ChatFormatting.BOLD);
        guiGraphics.drawString(this.font, title, left, 6, 0xFFFFFF);
        guiGraphics.fill(SIDEBAR_WIDTH+2, 20, guiGraphics.guiWidth(), 22, 0xC8262626);

        int top = 26;
        int referenceNum = 0;
        int spaceWidth = this.font.width(" ");

        for (int i = 0; i < this.currentPage.getParagraphs(); i++) {
            String paragraphUnformatted = this.currentPage.getParagraph(i).getString();

            List<Pair<Component, Boolean>> components = new ArrayList<>();
            String[] subs = paragraphUnformatted.split(" ");
            for (String sub : subs) {
                String[] candidate = sub.split(":");

                if (candidate.length == 2) {
                    ResourceLocation location = ResourceLocation.parse(sub);
                    WikiPage page = WikiableEntries.pageFor(location);
                    if (WikiableEntries.exists(location) && page != null) {
                        Component pageTitle = page.getTitle();
                        components.add(
                                new Pair<>(
                                        pageTitle.copy()
                                                .withStyle(ChatFormatting.AQUA)
                                                .withStyle(ChatFormatting.UNDERLINE),
                                        true
                                )
                        );
                        continue;
                    }
                }

                components.add(
                        new Pair<>(
                                Component.literal(ChatFormatting.RESET + sub),
                                false
                        )
                );
            }

            //I JUST COOKED SOME BUUUULLSHIT
            int y = top;
            int currentWidth = 0;
            int lines = 1;
            for (Pair<Component, Boolean> componentBooleanPair : components) {
                Component component = componentBooleanPair.getA();

                int componentWidth = this.font.width(component);
                currentWidth += componentWidth + spaceWidth;

                if (currentWidth >= pageWidth) {
                    y += 9;
                    currentWidth = componentWidth + spaceWidth;
                    lines += 1;
                }

                int offset = -(componentWidth+spaceWidth);
                if (componentBooleanPair.getB()) {
                    ReferenceButton ref = this.references.get(referenceNum);
                    ref.setX(left + 6 + currentWidth + offset);
                    ref.setY(y);
                    referenceNum += 1;
                }

                guiGraphics.drawString(this.font, component, left + 6 + currentWidth + offset, y, 0xFFFFFF);
            }

            top += lines*9+9;
        }
    }

    public class SidebarButton extends AbstractWidget {
        private final WikiPage wikiPage;

        public SidebarButton(WikiPage wikiPage) {
            super(0, 0, SIDEBAR_WIDTH-6, 16, wikiPage.getTitle());
            this.wikiPage = wikiPage;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int lineHeight = WikiScreen.this.font.lineHeight/2;
            guiGraphics.drawScrollingString(WikiScreen.this.font, this.getMessage(), this.getX()+19, this.getX()+this.getWidth()-3, this.getY()+this.getHeight()/2-lineHeight, 0xFFFFFF);

            guiGraphics.renderItem(new ItemStack(this.wikiPage.getItemlike()), this.getX(), this.getY());
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            WikiScreen.this.setPage(this.wikiPage);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }

    public class SidebarDropDown extends AbstractWidget {
        private boolean toggled = false;

        private final List<SidebarButton> subbuttons = new ArrayList<>();

        public SidebarDropDown(List<WikiPage> pages, Component message) {
            super(0, 0, SIDEBAR_WIDTH-6, 15, message);

            for (WikiPage page : pages) {
                SidebarButton button = new SidebarButton(page);
                button.active = false;
                button.visible = false;
                this.subbuttons.add(
                        WikiScreen.this.addWidget(button)
                );
            }
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int lineHeight = WikiScreen.this.font.lineHeight/2;
            guiGraphics.drawScrollingString(WikiScreen.this.font, this.getMessage(), this.getX()+19, this.getX()+this.getWidth()-3, this.getY()+this.getHeight()/2-lineHeight, 0xFFFFFF);

            int uOffset = this.toggled ? 16 : 0;
            guiGraphics.blit(EXTRA_SPRITES, this.getX()+5, this.getY()+5, uOffset, 0, 6, 6, 32, 16);
        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            this.setToggled(!this.toggled);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }

        public void addSubButtons(List<AbstractWidget> allSideWidgets) {
            if (this.toggled) {
                allSideWidgets.addAll(this.subbuttons);
            }
        }

        public void setToggled(boolean toggled) {
            this.toggled = toggled;
            this.subbuttons.forEach(subButton -> {
                subButton.visible = this.toggled;
                subButton.active = this.toggled;
            });
        }
    }

    public class ReferenceButton extends AbstractWidget {
        private final WikiPage referencePage;

        public ReferenceButton(WikiPage referencePage, int width, Component message) {
            super(0, 0, width, WikiScreen.this.font.lineHeight, message);
            this.referencePage = referencePage;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        }

        @Override
        public void onClick(double mouseX, double mouseY, int button) {
            WikiScreen.this.setPage(this.referencePage);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

        }
    }
}
