import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_scroll_block/settings_store.dart';

class ImportExportScreen extends StatefulWidget {
  final SettingStore store;

  const ImportExportScreen({Key? key, required this.store}) : super(key: key);

  @override
  _ImportExportScreenState createState() => _ImportExportScreenState();
}

class _ImportExportScreenState extends State<ImportExportScreen> {
  final TextEditingController _controller = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadCurrentSettings();
  }

  Future<void> _loadCurrentSettings() async {
    final json = await widget.store.exportToJson();
    _controller.text = json;
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Import/Export'),
        actions: [
          IconButton(
            icon: const Icon(Icons.copy),
            onPressed: () {
              Clipboard.setData(ClipboardData(text: _controller.text));
            },
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Expanded(
              child: TextField(
                controller: _controller,
                maxLines: null,
                expands: true,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  hintText: 'Paste JSON here to import, or copy current settings',
                ),
              ),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () async {
                try {
                  await widget.store.importFromJson(_controller.text);
                  Navigator.pop(context);
                } catch (e) {
                  // Silently fail, as requested
                }
              },
              child: const Text('Import'),
            ),
          ],
        ),
      ),
    );
  }
}
