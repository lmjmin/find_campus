import 'package:flutter/material.dart';

void main() {
  runApp(const FindCampusApp());
}

class FindCampusApp extends StatelessWidget {
  const FindCampusApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'FindCampus',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        scaffoldBackgroundColor: AppColors.background,
        colorScheme: ColorScheme.fromSeed(
          seedColor: AppColors.primary,
          primary: AppColors.primary,
          secondary: AppColors.indigo,
          surface: Colors.white,
        ),
        fontFamily: 'Arial',
        appBarTheme: const AppBarTheme(
          elevation: 0,
          centerTitle: false,
          backgroundColor: Colors.white,
          foregroundColor: AppColors.text,
          surfaceTintColor: Colors.white,
        ),
      ),
      home: const MainShell(),
    );
  }
}

class AppColors {
  static const primary = Color(0xFF2563EB);
  static const indigo = Color(0xFF4F46E5);
  static const background = Color(0xFFF5F7FB);
  static const text = Color(0xFF111827);
  static const subText = Color(0xFF6B7280);
  static const line = Color(0xFFE5E7EB);
  static const softBlue = Color(0xFFEEF2FF);
  static const green = Color(0xFF059669);
  static const yellow = Color(0xFFD97706);
  static const red = Color(0xFFDC2626);
}

class MainShell extends StatefulWidget {
  const MainShell({super.key});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int _index = 0;

  final List<Widget> _screens = const [
    HomeScreen(),
    LostListScreen(),
    FoundListScreen(),
    SearchScreen(),
    NotificationScreen(),
    MyPageScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(child: _screens[_index]),
      bottomNavigationBar: NavigationBar(
        height: 72,
        selectedIndex: _index,
        indicatorColor: AppColors.softBlue,
        onDestinationSelected: (value) => setState(() => _index = value),
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home),
            label: '??,
          ),
          NavigationDestination(
            icon: Icon(Icons.inventory_2_outlined),
            selectedIcon: Icon(Icons.inventory_2),
            label: 'Î∂ÑÏã§Î¨?,
          ),
          NavigationDestination(
            icon: Icon(Icons.backpack_outlined),
            selectedIcon: Icon(Icons.backpack),
            label: '?µÎìùÎ¨?,
          ),
          NavigationDestination(
            icon: Icon(Icons.search),
            selectedIcon: Icon(Icons.manage_search),
            label: 'Í≤Ä??,
          ),
          NavigationDestination(
            icon: Icon(Icons.notifications_none),
            selectedIcon: Icon(Icons.notifications),
            label: '?åÎ¶º',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outline),
            selectedIcon: Icon(Icons.person),
            label: 'ÎßàÏù¥',
          ),
        ],
      ),
    );
  }
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return AppPage(
      title: 'FindCampus',
      actions: [
        IconButton(
          onPressed: () {},
          icon: const Icon(Icons.notifications_none),
        ),
      ],
      child: ListView(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
        children: [
          const HeroPanel(),
          const SizedBox(height: 18),
          const StatRow(),
          const SizedBox(height: 24),
          SectionHeader(
            title: 'Îπ†Î•∏ Î©îÎâ¥',
            trailing: TextButton(onPressed: () {}, child: const Text('?ÑÏ≤¥')),
          ),
          const SizedBox(height: 12),
          const QuickMenuGrid(),
          const SizedBox(height: 28),
          const SectionHeader(title: 'ÏµúÍ∑º Î∂ÑÏã§Î¨?),
          const SizedBox(height: 12),
          ...sampleLostItems.take(2).map((item) => ItemCard(item: item)),
          const SizedBox(height: 20),
          const SectionHeader(title: 'ÏµúÍ∑º ?µÎìùÎ¨?),
          const SizedBox(height: 12),
          ...sampleFoundItems.take(2).map((item) => ItemCard(item: item)),
        ],
      ),
    );
  }
}

class HeroPanel extends StatelessWidget {
  const HeroPanel({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [AppColors.indigo, AppColors.primary],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(28),
        boxShadow: const [
          BoxShadow(
            color: Color(0x332563EB),
            blurRadius: 24,
            offset: Offset(0, 12),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity( 0.16),
              borderRadius: BorderRadius.circular(999),
            ),
            child: const Text(
              '?ôÎ™Ö?Ä?ôÍµê ?ôÏÉù ?ÑÏö© ?úÎπÑ??,
              style: TextStyle(
                color: Colors.white,
                fontSize: 13,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
          const SizedBox(height: 20),
          const Text(
            '?ÉÏñ¥Î≤ÑÎ¶∞ Î¨ºÍ±¥??nÎπ†Î•¥Í≤?Ï∞æÏïÑ?úÎ¶¥Í≤åÏöî',
            style: TextStyle(
              color: Colors.white,
              fontSize: 30,
              height: 1.25,
              fontWeight: FontWeight.w900,
            ),
          ),
          const SizedBox(height: 14),
          const Text(
            'Î∂ÑÏã§Î¨ºÍ≥º ?µÎìùÎ¨ºÏùÑ ?±Î°ù?òÍ≥†, Ï∫†Ìçº???àÏóê??Îπ†Î•¥Í≤??ïÏù∏?òÏÑ∏??',
            style: TextStyle(
              color: Color(0xFFEFF6FF),
              fontSize: 15,
              height: 1.5,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 22),
          Container(
            height: 52,
            padding: const EdgeInsets.symmetric(horizontal: 16),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(18),
            ),
            child: const Row(
              children: [
                Icon(Icons.search, color: AppColors.subText),
                SizedBox(width: 10),
                Expanded(
                  child: Text(
                    'Î∂ÑÏã§Î¨??êÎäî ?µÎìùÎ¨?Í≤Ä??,
                    style: TextStyle(
                      color: AppColors.subText,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                Icon(Icons.arrow_forward, color: AppColors.primary),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class StatRow extends StatelessWidget {
  const StatRow({super.key});

  @override
  Widget build(BuildContext context) {
    return const Row(
      children: [
        Expanded(child: StatCard(label: 'Î∂ÑÏã§Î¨?, value: '24Í±?)),
        SizedBox(width: 10),
        Expanded(child: StatCard(label: '?µÎìùÎ¨?, value: '50Í±?)),
        SizedBox(width: 10),
        Expanded(child: StatCard(label: 'Î∞òÌôò?ÑÎ£å', value: '12Í±?)),
      ],
    );
  }
}

class StatCard extends StatelessWidget {
  const StatCard({
    super.key,
    required this.label,
    required this.value,
  });

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.line),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: const TextStyle(
              color: AppColors.subText,
              fontSize: 12,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            value,
            style: const TextStyle(
              color: AppColors.text,
              fontSize: 20,
              fontWeight: FontWeight.w900,
            ),
          ),
        ],
      ),
    );
  }
}

class QuickMenuGrid extends StatelessWidget {
  const QuickMenuGrid({super.key});

  @override
  Widget build(BuildContext context) {
    return GridView.count(
      crossAxisCount: 3,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      mainAxisSpacing: 10,
      crossAxisSpacing: 10,
      childAspectRatio: 0.92,
      children: const [
        QuickMenuCard(
          icon: Icons.edit_note,
          title: 'Î∂ÑÏã§Î¨?n?±Î°ù',
          color: AppColors.primary,
        ),
        QuickMenuCard(
          icon: Icons.add_location_alt_outlined,
          title: '?µÎìùÎ¨?n?±Î°ù',
          color: AppColors.indigo,
        ),
        QuickMenuCard(
          icon: Icons.auto_awesome,
          title: '?†ÏÇ¨ Î¨ºÍ±¥\nÏ∂îÏ≤ú',
          color: Color(0xFF7C3AED),
        ),
      ],
    );
  }
}

class QuickMenuCard extends StatelessWidget {
  const QuickMenuCard({
    super.key,
    required this.icon,
    required this.title,
    required this.color,
  });

  final IconData icon;
  final String title;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.line),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 38,
            height: 38,
            decoration: BoxDecoration(
              color: color.withOpacity( 0.12),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(icon, color: color),
          ),
          const Spacer(),
          Text(
            title,
            style: const TextStyle(
              color: AppColors.text,
              fontSize: 14,
              height: 1.25,
              fontWeight: FontWeight.w900,
            ),
          ),
        ],
      ),
    );
  }
}

class LostListScreen extends StatelessWidget {
  const LostListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ItemListPage(
      title: 'Î∂ÑÏã§Î¨?,
      subtitle: '?ÉÏñ¥Î≤ÑÎ¶∞ Î¨ºÍ±¥??Ï∞æÏïÑÎ≥¥ÏÑ∏??,
      buttonLabel: 'Î∂ÑÏã§Î¨??±Î°ù',
      items: sampleLostItems,
      emptyHint: '?±Î°ù??Î∂ÑÏã§Î¨ºÏù¥ ?ÜÏñ¥??,
    );
  }
}

class FoundListScreen extends StatelessWidget {
  const FoundListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ItemListPage(
      title: '?µÎìùÎ¨?,
      subtitle: 'Î≥¥Í? Ï§ëÏù∏ Î¨ºÍ±¥???ïÏù∏?òÏÑ∏??,
      buttonLabel: '?µÎìùÎ¨??±Î°ù',
      items: sampleFoundItems,
      emptyHint: '?±Î°ù???µÎìùÎ¨ºÏù¥ ?ÜÏñ¥??,
    );
  }
}

class ItemListPage extends StatelessWidget {
  const ItemListPage({
    super.key,
    required this.title,
    required this.subtitle,
    required this.buttonLabel,
    required this.items,
    required this.emptyHint,
  });

  final String title;
  final String subtitle;
  final String buttonLabel;
  final List<ItemSummary> items;
  final String emptyHint;

  @override
  Widget build(BuildContext context) {
    return AppPage(
      title: title,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
        children: [
          PageIntro(title: title, subtitle: subtitle),
          const SizedBox(height: 14),
          PrimaryButton(
            label: buttonLabel,
            icon: Icons.add,
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => WriteScreen(title: buttonLabel)),
            ),
          ),
          const SizedBox(height: 18),
          const SearchField(hint: 'Î¨ºÍ±¥Î™? ?ÑÏπò, ?†ÏßúÎ°?Í≤Ä??),
          const SizedBox(height: 16),
          if (items.isEmpty)
            EmptyState(message: emptyHint)
          else
            ...items.map((item) => ItemCard(
                  item: item,
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => DetailScreen(item: item)),
                  ),
                )),
        ],
      ),
    );
  }
}

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key});

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  bool _showLost = true;

  @override
  Widget build(BuildContext context) {
    final items = _showLost ? sampleLostItems : sampleFoundItems;

    return AppPage(
      title: 'Í≤Ä??,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
        children: [
          const PageIntro(
            title: '?µÌï© Í≤Ä??,
            subtitle: 'Î∂ÑÏã§Î¨ºÍ≥º ?µÎìùÎ¨ºÏùÑ ??Î≤àÏóê Ï∞æÏïÑÎ≥¥ÏÑ∏??,
          ),
          const SizedBox(height: 16),
          const SearchField(hint: '?êÏñ¥?? ÏßÄÍ∞? ?ôÏÉùÏ¶? ?∞ÏÇ∞'),
          const SizedBox(height: 16),
          SegmentedButton<bool>(
            segments: const [
              ButtonSegment(value: true, label: Text('Î∂ÑÏã§Î¨?)),
              ButtonSegment(value: false, label: Text('?µÎìùÎ¨?)),
            ],
            selected: {_showLost},
            onSelectionChanged: (values) {
              setState(() => _showLost = values.first);
            },
          ),
          const SizedBox(height: 18),
          ...items.map((item) => ItemCard(item: item)),
        ],
      ),
    );
  }
}

class NotificationScreen extends StatelessWidget {
  const NotificationScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return AppPage(
      title: '?åÎ¶º',
      child: ListView(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
        children: const [
          PageIntro(
            title: '?åÎ¶º',
            subtitle: 'Îß§Ïπ≠, ?†Í≥† Ï≤òÎ¶¨, Î∞òÌôò ?àÎÇ¥Î•??ïÏù∏?òÏÑ∏??,
          ),
          SizedBox(height: 16),
          NoticeCard(
            title: '?†ÏÇ¨ ?µÎìùÎ¨ºÏù¥ Î∞úÍ≤¨?êÏñ¥??,
            body: 'Í≤Ä???∏Ìä∏Î∂?Í∞ÄÎ∞©Í≥º ÎπÑÏä∑???µÎìùÎ¨ºÏù¥ ?±Î°ù?òÏóà?µÎãà??',
            time: 'Î∞©Í∏à ??,
            unread: true,
          ),
          NoticeCard(
            title: 'Î∂ÑÏã§Î¨??±Î°ù ?ÑÎ£å',
            body: '?ëÏÑ±??Î∂ÑÏã§Î¨ºÏù¥ ?ïÏÉÅ ?±Î°ù?òÏóà?µÎãà??',
            time: '?¥Ï†ú',
            unread: false,
          ),
        ],
      ),
    );
  }
}

class MyPageScreen extends StatelessWidget {
  const MyPageScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return AppPage(
      title: 'ÎßàÏù¥?òÏù¥ÏßÄ',
      child: ListView(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
        children: [
          Container(
            padding: const EdgeInsets.all(18),
            decoration: cardDecoration(),
            child: const Row(
              children: [
                CircleAvatar(
                  radius: 28,
                  backgroundColor: AppColors.softBlue,
                  child: Icon(Icons.person, color: AppColors.primary),
                ),
                SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'ÍπÄÎ∂ÑÏã§',
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                      SizedBox(height: 4),
                      Text(
                        '?ôÎ™Ö?Ä?ôÍµê ?ôÏÉù',
                        style: TextStyle(
                          color: AppColors.subText,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 18),
          const MenuTile(icon: Icons.inventory_2_outlined, label: '??Î∂ÑÏã§Î¨?),
          const MenuTile(icon: Icons.backpack_outlined, label: '???µÎìùÎ¨?),
          const MenuTile(icon: Icons.notifications_none, label: '?åÎ¶º ?§Ï†ï'),
          const MenuTile(icon: Icons.settings_outlined, label: '?òÍ≤Ω ?§Ï†ï'),
        ],
      ),
    );
  }
}

class DetailScreen extends StatelessWidget {
  const DetailScreen({super.key, required this.item});

  final ItemSummary item;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('?ÅÏÑ∏'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Container(
            height: 220,
            decoration: BoxDecoration(
              gradient: const LinearGradient(
                colors: [AppColors.softBlue, Color(0xFFE0E7FF)],
              ),
              borderRadius: BorderRadius.circular(26),
            ),
            child: Icon(item.icon, size: 84, color: AppColors.primary),
          ),
          const SizedBox(height: 18),
          Container(
            padding: const EdgeInsets.all(18),
            decoration: cardDecoration(),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                StatusBadge(status: item.status),
                const SizedBox(height: 14),
                Text(
                  item.title,
                  style: const TextStyle(
                    fontSize: 24,
                    height: 1.25,
                    fontWeight: FontWeight.w900,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  item.description,
                  style: const TextStyle(
                    color: AppColors.subText,
                    height: 1.6,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 18),
                InfoRow(icon: Icons.location_on_outlined, text: item.location),
                InfoRow(icon: Icons.schedule, text: item.date),
                InfoRow(icon: Icons.category_outlined, text: item.category),
              ],
            ),
          ),
          const SizedBox(height: 18),
          PrimaryButton(
            label: item.type == ItemType.lost ? '?†ÏÇ¨ ?µÎìùÎ¨??ïÏù∏?òÍ∏∞' : '??Î¨ºÍ±¥?ºÎ°ú ?òÎ†π ?†Ï≤≠',
            icon: item.type == ItemType.lost ? Icons.search : Icons.handshake,
            onPressed: () {},
          ),
        ],
      ),
    );
  }
}

class WriteScreen extends StatelessWidget {
  const WriteScreen({super.key, required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(title: Text(title)),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          const InputLabel('Î¨ºÍ±¥Î™?),
          const AppTextField(hint: '?? ?êÏñ¥?? ÏßÄÍ∞? ?ôÏÉùÏ¶?),
          const SizedBox(height: 14),
          const InputLabel('?ÑÏπò'),
          const AppTextField(hint: '?? Ï§ëÏïô?ÑÏÑúÍ¥Ä 1Ï∏?),
          const SizedBox(height: 14),
          const InputLabel('?ÅÏÑ∏ ?§Î™Ö'),
          const AppTextField(
            hint: '?âÏÉÅ, Î∏åÎûú?? ?πÏßï???êÏÑ∏???ÅÏñ¥Ï£ºÏÑ∏??,
            maxLines: 5,
          ),
          const SizedBox(height: 22),
          PrimaryButton(
            label: '?±Î°ù?òÍ∏∞',
            icon: Icons.check,
            onPressed: () {},
          ),
        ],
      ),
    );
  }
}

class AppPage extends StatelessWidget {
  const AppPage({
    super.key,
    required this.title,
    required this.child,
    this.actions,
  });

  final String title;
  final Widget child;
  final List<Widget>? actions;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        titleSpacing: 20,
        title: Row(
          children: [
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: AppColors.softBlue,
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(
                Icons.travel_explore,
                color: AppColors.primary,
                size: 20,
              ),
            ),
            const SizedBox(width: 10),
            Text(
              title,
              style: const TextStyle(
                fontWeight: FontWeight.w900,
                color: AppColors.primary,
              ),
            ),
          ],
        ),
        actions: actions,
      ),
      body: child,
    );
  }
}

class PageIntro extends StatelessWidget {
  const PageIntro({
    super.key,
    required this.title,
    required this.subtitle,
  });

  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(
            fontSize: 28,
            height: 1.2,
            color: AppColors.text,
            fontWeight: FontWeight.w900,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          subtitle,
          style: const TextStyle(
            color: AppColors.subText,
            fontSize: 15,
            height: 1.5,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }
}

class SectionHeader extends StatelessWidget {
  const SectionHeader({
    super.key,
    required this.title,
    this.trailing,
  });

  final String title;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Text(
            title,
            style: const TextStyle(
              color: AppColors.text,
              fontSize: 20,
              fontWeight: FontWeight.w900,
            ),
          ),
        ),
        if (trailing != null) trailing!,
      ],
    );
  }
}

class SearchField extends StatelessWidget {
  const SearchField({super.key, required this.hint});

  final String hint;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 54,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: AppColors.line),
      ),
      child: Row(
        children: [
          const Icon(Icons.search, color: AppColors.subText),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              hint,
              style: const TextStyle(
                color: AppColors.subText,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class ItemCard extends StatelessWidget {
  const ItemCard({
    super.key,
    required this.item,
    this.onTap,
  });

  final ItemSummary item;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(20),
        child: Container(
          padding: const EdgeInsets.all(14),
          decoration: cardDecoration(),
          child: Row(
            children: [
              Container(
                width: 64,
                height: 64,
                decoration: BoxDecoration(
                  color: AppColors.softBlue,
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Icon(item.icon, color: AppColors.primary, size: 30),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        StatusBadge(status: item.status),
                        const SizedBox(width: 8),
                        Text(
                          item.type == ItemType.lost ? 'Î∂ÑÏã§Î¨? : '?µÎìùÎ¨?,
                          style: const TextStyle(
                            color: AppColors.subText,
                            fontSize: 12,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      item.title,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: AppColors.text,
                        fontSize: 16,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      '${item.location} - ${item.date}',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: AppColors.subText,
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
              const Icon(Icons.chevron_right, color: AppColors.subText),
            ],
          ),
        ),
      ),
    );
  }
}

class StatusBadge extends StatelessWidget {
  const StatusBadge({super.key, required this.status});

  final String status;

  @override
  Widget build(BuildContext context) {
    final color = switch (status) {
      'Î∞òÌôò?ÑÎ£å' => AppColors.green,
      'Î≥¥Í?Ï§? => AppColors.yellow,
      _ => AppColors.primary,
    };

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 5),
      decoration: BoxDecoration(
        color: color.withOpacity( 0.12),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Text(
        status,
        style: TextStyle(
          color: color,
          fontSize: 11,
          fontWeight: FontWeight.w900,
        ),
      ),
    );
  }
}

class NoticeCard extends StatelessWidget {
  const NoticeCard({
    super.key,
    required this.title,
    required this.body,
    required this.time,
    required this.unread,
  });

  final String title;
  final String body;
  final String time;
  final bool unread;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: cardDecoration(
        borderColor: unread ? AppColors.primary : AppColors.line,
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: AppColors.softBlue,
              borderRadius: BorderRadius.circular(14),
            ),
            child: const Icon(Icons.notifications, color: AppColors.primary),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontWeight: FontWeight.w900,
                    fontSize: 16,
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  body,
                  style: const TextStyle(
                    color: AppColors.subText,
                    height: 1.45,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  time,
                  style: const TextStyle(
                    color: AppColors.subText,
                    fontSize: 12,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class MenuTile extends StatelessWidget {
  const MenuTile({
    super.key,
    required this.icon,
    required this.label,
  });

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 10),
      decoration: cardDecoration(),
      child: ListTile(
        leading: Icon(icon, color: AppColors.primary),
        title: Text(
          label,
          style: const TextStyle(fontWeight: FontWeight.w800),
        ),
        trailing: const Icon(Icons.chevron_right),
      ),
    );
  }
}

class InfoRow extends StatelessWidget {
  const InfoRow({
    super.key,
    required this.icon,
    required this.text,
  });

  final IconData icon;
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 10),
      child: Row(
        children: [
          Icon(icon, color: AppColors.primary, size: 20),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              text,
              style: const TextStyle(
                color: AppColors.subText,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class InputLabel extends StatelessWidget {
  const InputLabel(this.text, {super.key});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(
        text,
        style: const TextStyle(
          color: AppColors.text,
          fontWeight: FontWeight.w900,
        ),
      ),
    );
  }
}

class AppTextField extends StatelessWidget {
  const AppTextField({
    super.key,
    required this.hint,
    this.maxLines = 1,
  });

  final String hint;
  final int maxLines;

  @override
  Widget build(BuildContext context) {
    return TextField(
      maxLines: maxLines,
      decoration: InputDecoration(
        hintText: hint,
        filled: true,
        fillColor: Colors.white,
        contentPadding: const EdgeInsets.all(16),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: AppColors.line),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: AppColors.line),
        ),
      ),
    );
  }
}

class PrimaryButton extends StatelessWidget {
  const PrimaryButton({
    super.key,
    required this.label,
    required this.icon,
    required this.onPressed,
  });

  final String label;
  final IconData icon;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return FilledButton.icon(
      onPressed: onPressed,
      icon: Icon(icon),
      label: Text(label),
      style: FilledButton.styleFrom(
        minimumSize: const Size.fromHeight(54),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        textStyle: const TextStyle(fontSize: 15, fontWeight: FontWeight.w900),
      ),
    );
  }
}

class EmptyState extends StatelessWidget {
  const EmptyState({super.key, required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(28),
      decoration: cardDecoration(),
      child: Column(
        children: [
          const Icon(Icons.inbox_outlined, color: AppColors.subText, size: 42),
          const SizedBox(height: 10),
          Text(
            message,
            style: const TextStyle(
              color: AppColors.subText,
              fontWeight: FontWeight.w800,
            ),
          ),
        ],
      ),
    );
  }
}

BoxDecoration cardDecoration({Color borderColor = AppColors.line}) {
  return BoxDecoration(
    color: Colors.white,
    borderRadius: BorderRadius.circular(20),
    border: Border.all(color: borderColor),
    boxShadow: const [
      BoxShadow(
        color: Color(0x0F0F172A),
        blurRadius: 18,
        offset: Offset(0, 8),
      ),
    ],
  );
}

enum ItemType { lost, found }

class ItemSummary {
  const ItemSummary({
    required this.type,
    required this.title,
    required this.description,
    required this.location,
    required this.date,
    required this.category,
    required this.status,
    required this.icon,
  });

  final ItemType type;
  final String title;
  final String description;
  final String location;
  final String date;
  final String category;
  final String status;
  final IconData icon;
}

const sampleLostItems = [
  ItemSummary(
    type: ItemType.lost,
    title: 'Í≤Ä???∏Ìä∏Î∂?Í∞ÄÎ∞?,
    description: 'Îß•Î∂Å ?ÑÎ°ú 13?∏Ïπò?Ä Ï∂©Ï†ÑÍ∏∞Í? ?§Ïñ¥?àÎäî Í≤Ä?ïÏÉâ ?∏Ìä∏Î∂?Í∞ÄÎ∞©ÏûÖ?àÎã§.',
    location: 'Ï§ëÏïô?ÑÏÑúÍ¥Ä 2Ï∏?,
    date: '?§Îäò 14:30',
    category: 'Í∞ÄÎ∞?,
    status: '?ëÏàòÏ§?,
    icon: Icons.business_center,
  ),
  ItemSummary(
    type: ItemType.lost,
    title: '?ôÏÉùÏ¶?,
    description: '2024?ÑÎèÑ ?ÖÌïô ?ôÏÉùÏ¶ùÏûÖ?àÎã§. ICT 1Í¥Ä Í∑ºÏ≤ò?êÏÑú Î∂ÑÏã§?àÏäµ?àÎã§.',
    location: 'ICT 1Í¥Ä 3Ï∏?,
    date: '?§Îäò 11:00',
    category: '?†Î∂ÑÏ¶?,
    status: '?ëÏàòÏ§?,
    icon: Icons.badge,
  ),
  ItemSummary(
    type: ItemType.lost,
    title: '?êÏñ¥???ÑÎ°ú',
    description: '?∞ÏÉâ ?êÏñ¥??ÏºÄ?¥Ïä§ ?¨Ìï®?ÖÎãà??',
    location: '?ôÎ™Ö?§Ì??îÏ?',
    date: '?¥Ï†ú',
    category: '?ÑÏûêÍ∏∞Í∏∞',
    status: 'Î≥¥Í?Ï§?,
    icon: Icons.headphones,
  ),
];

const sampleFoundItems = [
  ItemSummary(
    type: ItemType.found,
    title: '?êÏñ¥???µÎìù',
    description: '?∞ÏÉâ ?êÏñ¥??ÏºÄ?¥Ïä§ ?¨Ìï®, Î∂ÑÏã§Î¨??ºÌÑ∞??Î≥¥Í? Ï§ëÏûÖ?àÎã§.',
    location: 'Í±¥Ï∂ï?îÏûê?∏Í? 6Ï∏?,
    date: '?§Îäò 09:15',
    category: '?ÑÏûêÍ∏∞Í∏∞',
    status: 'Î≥¥Í?Ï§?,
    icon: Icons.headphones,
  ),
  ItemSummary(
    type: ItemType.found,
    title: 'Í∞àÏÉâ ÏßÄÍ∞?Î∞úÍ≤¨',
    description: 'Í∞àÏÉâ Í∞ÄÏ£?ÏßÄÍ∞ëÏù¥Î©??ôÏÉùÏ¶ùÏù¥ ?¨Ìï®?òÏñ¥ ?àÏäµ?àÎã§.',
    location: '?Ä?ôÎ≥∏Î∂Ä ??,
    date: '?§Îäò 10:30',
    category: 'ÏßÄÍ∞?,
    status: 'Î≥¥Í?Ï§?,
    icon: Icons.account_balance_wallet,
  ),
  ItemSummary(
    type: ItemType.found,
    title: '?∞ÏÇ∞ ?µÎìù',
    description: 'Í≤Ä?ïÏÉâ ?•Ïö∞?∞ÏûÖ?àÎã§.',
    location: '?ôÏÉù?åÍ? 1Ï∏?,
    date: '?¥Ï†ú',
    category: '?ùÌôú?©Ìíà',
    status: 'Î∞òÌôò?ÑÎ£å',
    icon: Icons.umbrella,
  ),
];

