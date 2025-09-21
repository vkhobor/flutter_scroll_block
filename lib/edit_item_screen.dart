import 'package:flutter/material.dart';
import 'package:flutter_scroll_block/settings_store.dart';

class EditItemScreen extends StatefulWidget {
  final ListItem item;
  final Function(ListItem) onEdit;

  EditItemScreen({Key? key, required this.item, required this.onEdit})
    : super(key: key);

  @override
  _EditItemScreenState createState() => _EditItemScreenState();
}

class _EditItemScreenState extends State<EditItemScreen> {
  late TextEditingController appidController;
  late TextEditingController viewidController;
  late bool usePolling;
  late bool immediateBlock;

  @override
  void initState() {
    super.initState();
    appidController = TextEditingController(text: widget.item.appid);
    viewidController = TextEditingController(text: widget.item.viewid);
    usePolling = widget.item.usePolling;
    immediateBlock = widget.item.immediateBlock;
  }

  @override
  void dispose() {
    appidController.dispose();
    viewidController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Edit Item')),
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
                final updatedItem = ListItem(
                  appid: appidController.text,
                  viewid: viewidController.text,
                  enabled: widget.item.enabled,
                  usePolling: usePolling,
                  immediateBlock: immediateBlock,
                );
                widget.onEdit(updatedItem);
                Navigator.pop(context);
              },
              child: const Text('Save Changes'),
            ),
          ],
        ),
      ),
    );
  }
}
