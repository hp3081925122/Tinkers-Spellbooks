# Tinkers' Spellbooks（匠魂魔导书与法杖）

Minecraft 1.20.1 Forge 平台下 **Tinkers' Construct（匠魂 3）** 与 **Iron's Spells 'n Spellbooks（铁魔法）** 的模块化联动附属模组。

## 核心内容

### 1. 模块化施法装备
- **奥术法杖（`tinkers_spellbooks:arcane_staff`）**：由 `杖首核心` + `导魔杖杆` + `奥术束件` 组装。主手持有提供法术强度、最大法力、冷却缩减与吟唱缩减加成；右键直接吟唱并施放当前选中的法术，支持在奥术铁砧中灌注法术卷轴（最大灌注槽位随材料与扩容强化成长）及镶嵌升级法球。
- **魔导典籍（`tinkers_spellbooks:modular_spellbook`）**：由 `魔导书皮` + `铭咒书页` + `奥术束件` 组装。可穿戴于 Curios `spellbook` 魔法书栏位并显示腰间挂书模型，支持在 **抄写台（Inscription Table）** 与 **奥术铁砧（Arcane Anvil）** 中刻写、拆卸法术卷轴、镶嵌升级法球与扩容槽位，同时支持放置于讲台翻阅。

### 2. 五大魔法部件与全材料自动兼容
- 注册 5 种魔法部件属性类型：`staff_head`、`staff_handle`、`book_cover`、`spell_pages`、`arcane_binding`（均配备金质、沙子、红沙铸模与部件制造台配方）。
- 内置自动兼容推导与材质贴图动态生成器：原版匠魂及任意第三方匠魂附属的材料无需额外适配即可直接制造全部魔法部件、同步客户端纹理并在匠魂四本百科书中查阅属性。

### 3. 铁魔法专属材料与强化词条
- 新增 11 种铁魔法匠魂材料：`奥术布匹`、`野猪皮`、`冰霜杖柄`、`奥术合金`、`染血羊皮纸`、`神圣珍珠`、`永冻晶簇`、`秘银`、`炎金`、`末影龙皮`、`深渊手稿`。
- 新增九大学派符文聚焦升级、通用法力/咏唱/护法升级，以及 `魔典扩容`、`奥术超载`、`大魔导真知`（全法术有效等级 +1）能力词条。

## 构建验证

```powershell
$env:JAVA_HOME = 'C:\Users\30819\.jdks\ms-17.0.18'
.\gradlew.bat compileJava processResources
```
