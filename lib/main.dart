import 'package:flutter/material.dart';
import 'package:flutter_scroll_block/edit_item_screen.dart';
import 'package:flutter_scroll_block/settings_store.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:shared_preferences_android/shared_preferences_android.dart';

const SharedPreferencesAsyncAndroidOptions options =
    SharedPreferencesAsyncAndroidOptions(
      backend: SharedPreferencesAndroidBackendLibrary.SharedPreferences,
      originalSharedPreferencesOptions: AndroidSharedPreferencesStoreOptions(),
    );

class AddItemScreen extends StatefulWidget {
  final Function(ListItem) onAdd;

  const AddItemScreen({Key? key, required this.onAdd}) : super(key: key);

  @override
  _AddItemScreenState createState() => _AddItemScreenState();
}

class _AddItemScreenState extends State<AddItemScreen> {
  final TextEditingController appidController = TextEditingController();
  final TextEditingController viewidController = TextEditingController();

  bool usePolling = false;
  bool immediateBlock = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Add New Item')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            TextField(
              controller: appidController,
              decoration: const InputDecoration(labelText: 'App ID'),
            ),
            TextField(
              controller: viewidController,
              decoration: const InputDecoration(labelText: 'View ID'),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Use Polling'),
                Switch(
                  value: usePolling,
                  onChanged: (value) {
                    setState(() {
                      usePolling = value;
                    });
                  },
                ),
              ],
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Immediate Block'),
                Switch(
                  value: immediateBlock,
                  onChanged: (value) {
                    setState(() {
                      immediateBlock = value;
                    });
                  },
                ),
              ],
            ),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () {
                final newItem = ListItem(
                  appid: appidController.text,
                  viewid: viewidController.text,
                  enabled: true,
                  usePolling: usePolling,
                  immediateBlock: immediateBlock,
                );
                widget.onAdd(newItem);
                Navigator.pop(context);
              },
              child: const Text('Add Item'),
            ),
          ],
        ),
      ),
    );
  }
}

void main() {
  runApp(const MainApp());
}

class ListScreen extends StatefulWidget {
  final SettingStore store;

  ListScreen({required this.store});

  @override
  _ListScreenState createState() => _ListScreenState();
}

class _ListScreenState extends State<ListScreen> {
  @override
  void initState() {
    super.initState();
    _initializeData();
  }

  late VoidCallback listener;

  Future<void> _initializeData() async {
    listener = () {
      setState(() {});
    };
    widget.store.addListener(listener);

    await widget.store.init();
  }

  @override
  void dispose() {
    widget.store.removeListener(listener);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Stack(children: [_buildListView(), _buildFloatingActionButton()]);
  }

  Widget _buildListView() {
    return ListView.builder(
      itemCount: widget.store.items.length,
      itemBuilder: (context, index) {
        final item = widget.store.items[index];
        return Dismissible(
          key: Key(item.appid),
          direction: DismissDirection.endToStart,
          onDismissed: (direction) async {
            // HACK: it removes it optimistically anyway, and async gap happens on await otherwise can cause ui glitches
            final future = widget.store.deleteItem(index);
            setState(() {});
            await future;
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(content: Text('Item ${item.appid} deleted')),
            );
          },
          background: Container(
            color: Colors.red,
            alignment: Alignment.centerRight,
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: const Icon(Icons.delete, color: Colors.white),
          ),
          child: ListTile(
            title: Text('App ID: ${item.appid}'),
            subtitle: Text('View ID: ${item.viewid}'),
            trailing: Switch(
              value: item.enabled,
              onChanged: (value) async {
                widget.store.updateItem(
                  index,
                  ListItem(
                    appid: item.appid,
                    viewid: item.viewid,
                    enabled: value,
                    usePolling: item.usePolling,
                    immediateBlock: item.immediateBlock,
                  ),
                );
                setState(() {});
              },
            ),
            onTap: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => EditItemScreen(
                    item: item,
                    onEdit: (updatedItem) async {
                      widget.store.updateItem(index, updatedItem);
                    },
                  ),
                ),
              );
            },
          ),
        );
      },
    );
  }

  Widget _buildFloatingActionButton() {
    return Positioned(
      bottom: 16,
      right: 16,
      child: FloatingActionButton(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => AddItemScreen(
                onAdd: (newItem) async {
                  await widget.store.addItem(newItem);
                  setState(() {});
                },
              ),
            ),
          );
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}

class MainApp extends StatefulWidget {
  const MainApp({super.key});

  @override
  State<MainApp> createState() => _MainAppState();
}

class _MainAppState extends State<MainApp> {
  late SettingStore settings;

  @override
  void initState() {
    super.initState();
    settings = SettingStore(SharedPreferencesAsync(options: options));
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('List Manager')),
        body: ListScreen(store: settings),
      ),
    );
  }
}
