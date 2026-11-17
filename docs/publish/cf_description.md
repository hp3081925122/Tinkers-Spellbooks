# Tinkers' Spellbooks

A **Tinkers' Construct** & **Iron's Spells 'n Spellbooks** integration addon for Minecraft 1.20.1 Forge, introducing modular spellbooks and staves with full Tinkers material compatibility.

## Features

### 1. Modular Spellcasting Gear
- **Arcane Staff (`tinkers_spellbooks:arcane_staff`)**: Assembled from **Staff Head**, **Staff Handle**, and **Arcane Binding**. Provides Spell Power, Max Mana, Cooldown Reduction, and Cast Time Reduction when held in either the main hand or offhand (only the main-hand staff takes effect when dual-wielded). Right-click to cast the selected spell directly, imbue scrolls or apply upgrade orbs at an Arcane Anvil. Consumes 1 durability per cast by default (configurable).
- **Modular Spellbook (`tinkers_spellbooks:modular_spellbook`)**: Assembled from **Book Cover**, **Spell Pages**, and **Arcane Binding**. Equipped in the Curios spellbook slot with a 3D waist model and lectern readability. Has no durability limit and grants its stats strictly when equipped in the spellbook slot. Supports inscribing and managing spell scrolls at both the Inscription Table and Arcane Anvil, with spell capacity scaling dynamically with page material and modifiers.

### 2. Five Magical Tool Parts & Universal Material Compatibility
- Adds 5 magical part types (`staff_head`, `staff_handle`, `book_cover`, `spell_pages`, `arcane_binding`) along with gold, sand, and red sand casts.
- Built-in automatic fallback generator derives part stats and dynamic textures for vanilla Tinkers materials and third-party Tinkers addons out of the box, complete with Tinkers book entries.

### 3. Iron's Spells Materials & Modifiers
- **11 New Materials**: Arcane Cloth, Hogskin, Frosted Helve, Arcane Ingot, Bloody Vellum, Divine Pearl, Permafrost, Mithril, Pyrium, Dragonskin, and Eldritch Manuscript.
- **12 Spellcasting Upgrades**: School Focuses (Fire, Ice, Lightning, Holy, Ender, Blood, Evocation, Nature, Eldritch), Mana Reservoir, Quick Chant, and Warded Script.
- **3 Spellcasting Abilities**: Grimoire Expansion, Arcane Overdrive, and Archmage Insight (+1 Effective Spell Level).

## Dependencies
- Minecraft 1.20.1 (Forge)
- Tinkers' Construct & Mantle
- Iron's Spells 'n Spellbooks
- Curios API
- GeckoLib

---

# Tinkers' Spellbooks（匠魂魔导书与法杖）

面向 Minecraft 1.20.1 Forge 的 **Tinkers' Construct（匠魂 3）** 与 **Iron's Spells 'n Spellbooks（铁魔法）** 模块化联动附属模组。

## 核心内容

### 1. 模块化施法装备
- **导魔长杖（`tinkers_spellbooks:arcane_staff`）**：由「杖首核心 + 导魔杖杆 + 奥术束件」组装。主手或副手持有均提供法术强度、最大法力、冷却缩减与吟唱缩减加成（双持时仅生效主手一把）；右键直接吟唱并施放当前选中的法术，支持在奥术铁砧中灌注法术卷轴或镶嵌升级法球。默认每次施法消耗 1 点耐久（可在配置中关闭）。
- **魔导典籍（`tinkers_spellbooks:modular_spellbook`）**：由「魔导书皮 + 铭咒书页 + 奥术束件」组装。可穿戴于 Curios 魔法书饰品栏并显示 3D 挂书模型，支持放置于讲台翻阅。无耐久度限制，属性与词条仅在穿戴于魔法书饰品栏时生效；支持在抄写台与奥术铁砧中刻写和管理法术卷轴，法术槽位容量随书页材质与扩容强化动态成长。

### 2. 五大魔法部件与全材料自动兼容
- 新增 5 种魔法部件（`staff_head`、`staff_handle`、`book_cover`、`spell_pages`、`arcane_binding`）及对应的金质、沙子、红沙铸模与部件制造台配方。
- 内置自动兼容与动态贴图生成器：原版匠魂及任意第三方匠魂附属的材料无需额外适配即可直接制造全部魔法部件，并在《材料与你》《匠魂百科全书》中查阅对应属性。

### 3. 铁魔法专属材料与独立强化分类
- **11 种铁魔法材料**：奥术布匹、野猪皮、冰霜杖柄、奥术合金、染血羊皮纸、神圣珍珠、永冻晶簇、秘银、炎金、末影龙皮、深渊手稿。
- **12 种施法升级**：九大学派符文聚焦、魔力储罐、急速咏唱、护法铭文。
- **3 种施法能力**：魔典扩容、奥术超载、大魔导真知（全法术有效等级 +1）。

## 前置依赖
- Minecraft 1.20.1（Forge）
- Tinkers' Construct（匠魂 3）与 Mantle
- Iron's Spells 'n Spellbooks（铁魔法）
- Curios API
- GeckoLib
