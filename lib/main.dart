import 'package:flutter/material.dart';
import 'package:flutter_scroll_block/add_item_screen.dart';
import 'package:flutter_scroll_block/edit_item_screen.dart';
import 'package:flutter_scroll_block/import_export_screen.dart';
import 'package:flutter_scroll_block/settings_store.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:shared_preferences_android/shared_preferences_android.dart';
import 'package:flutter/services.dart';

const SharedPreferencesAsyncAndroidOptions options =
    SharedPreferencesAsyncAndroidOptions(
      backend: SharedPreferencesAndroidBackendLibrary.SharedPreferences,
      originalSharedPreferencesOptions: AndroidSharedPreferencesStoreOptions(),
    );

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
    return Scaffold(
      appBar: AppBar(
        title: const Text('Doom scroll blocklist'),
        actions: [
          IconButton(
            icon: const Icon(Icons.import_export),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => ImportExportScreen(store: widget.store),
                ),
              );
            },
          ),
        ],
      ),
      body: Stack(children: [_buildListView(), _buildFloatingActionButton()]),
    );
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
      home: ListScreen(store: settings),
    );
  }
}
