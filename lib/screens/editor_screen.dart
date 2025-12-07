import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../models/blind_level.dart';
import '../data/default_structure.dart';
import '../widgets/smart_input_popup.dart';
import '../widgets/confirm_popup.dart';
import '../widgets/add_level_popup.dart';
import '../widgets/focusable_button.dart';
import 'timer_screen.dart';

class EditorScreen extends StatefulWidget {
  final List<BlindLevel>? initialStructure;

  const EditorScreen({super.key, this.initialStructure});

  @override
  State<EditorScreen> createState() => _EditorScreenState();
}

class _EditorScreenState extends State<EditorScreen> {
  late List<BlindLevel> structure;
  late List<FocusNode> _rowFocusNodes;
  int _selectedRowIndex = 0;
  int _selectedColumnIndex = 0; // 0=SB, 1=TEMPO, 2=CESTINO
  bool _isEditingRow = false; // true quando sei dentro una riga a modificare

  @override
  void initState() {
    super.initState();
    structure = widget.initialStructure ?? getDefaultStructure();
    _rowFocusNodes = List.generate(structure.length, (_) => FocusNode());
  }

  @override
  void dispose() {
    for (var node in _rowFocusNodes) {
      node.dispose();
    }
    super.dispose();
  }

  void _updateFocusNodes() {
    // Aggiorna la lista di focus nodes quando la struttura cambia
    for (var node in _rowFocusNodes) {
      node.dispose();
    }
    _rowFocusNodes = List.generate(structure.length, (_) => FocusNode());
  }

  void _editSmallBlind(int index) {
    final level = structure[index];

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => SmartInputPopup(
        title: 'SMALL BLIND - LV.${level.levelNumber}',
        currentValue: level.smallBlind,
        isBlind: true,
        structure: structure,
        currentIndex: index,
        onConfirm: (newValue) {
          setState(() {
            // Aggiorna SB e automaticamente BB = SB × 2
            structure[index] = level.copyWith(
              smallBlind: newValue,
              bigBlind: newValue * 2,
            );
          });
          Navigator.of(context).pop();
        },
      ),
    );
  }

  void _editDuration(int index) {
    final level = structure[index];

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => SmartInputPopup(
        title: 'DURATA - LV.${level.levelNumber}',
        currentValue: level.durationMinutes,
        isBlind: false,
        structure: structure,
        currentIndex: index,
        onConfirm: (newValue) {
          setState(() {
            structure[index] = level.copyWith(durationMinutes: newValue);
          });
          Navigator.of(context).pop();
        },
      ),
    );
  }

  void _deleteLevel(int index) {
    final level = structure[index];
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => ConfirmPopup(
        title: 'Eliminare il livello ${level.levelNumber}?',
        subtitle: '(${level.smallBlind} / ${level.bigBlind})',
        onConfirm: () {
          setState(() {
            structure.removeAt(index);
            // Rinumera i livelli
            for (int i = 0; i < structure.length; i++) {
              structure[i] = structure[i].copyWith(levelNumber: i + 1);
            }
            // Aggiorna focus nodes e indice selezionato
            _updateFocusNodes();
            if (_selectedRowIndex >= structure.length && structure.isNotEmpty) {
              _selectedRowIndex = structure.length - 1;
            }
            _isEditingRow = false;
            _selectedColumnIndex = 0;
          });
          Navigator.of(context).pop();
        },
        onCancel: () {
          Navigator.of(context).pop();
        },
      ),
    );
  }

  void _addLevel() {
    final lastLevel = structure.isNotEmpty ? structure.last : null;
    final defaultSB = lastLevel != null ? lastLevel.smallBlind * 2 : 100;
    final defaultBB = lastLevel != null ? lastLevel.bigBlind * 2 : 200;
    final defaultDuration = lastLevel?.durationMinutes ?? 15;

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => AddLevelPopup(
        levelNumber: structure.length + 1,
        defaultSmallBlind: defaultSB,
        defaultBigBlind: defaultBB,
        defaultDuration: defaultDuration,
        onAdd: (sb, bb, duration) {
          setState(() {
            structure.add(BlindLevel(
              levelNumber: structure.length + 1,
              smallBlind: sb,
              bigBlind: bb,
              durationMinutes: duration,
            ));
            _updateFocusNodes();
          });
          Navigator.of(context).pop();
        },
      ),
    );
  }

  void _startTimer() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (context) => TimerScreen(structure: structure),
      ),
    );
  }

  Widget _buildTableHeader() {
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 20),
      decoration: BoxDecoration(
        border: Border(
          bottom: BorderSide(
            color: theme.colorScheme.onSurface.withOpacity(0.2),
            width: 2,
          ),
        ),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            flex: 2,
            child: Text(
              'LV',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.bold,
                color: theme.colorScheme.onSurface.withOpacity(0.7),
              ),
            ),
          ),
          Expanded(
            flex: 3,
            child: Text(
              'SB',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.bold,
                color: theme.colorScheme.onSurface.withOpacity(0.7),
              ),
            ),
          ),
          Expanded(
            flex: 3,
            child: Text(
              'BB',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.bold,
                color: theme.colorScheme.onSurface.withOpacity(0.7),
              ),
            ),
          ),
          Expanded(
            flex: 3,
            child: Text(
              'TEMPO',
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.bold,
                color: theme.colorScheme.onSurface.withOpacity(0.7),
              ),
            ),
          ),
          Expanded(
            flex: 2,
            child: Align(
              alignment: Alignment.centerRight,
              child: Icon(
                Icons.delete_outline,
                size: 20,
                color: theme.colorScheme.onSurface.withOpacity(0.3),
              ),
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.of(context).pop(),
        ),
        title: const Text('STRUTTURA TORNEO'),
      ),
      body: Column(
        children: [
          Expanded(
            child: Center(
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 1800),
                child: Column(
                  children: [
                    // Header della tabella
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 8),
                      child: _buildTableHeader(),
                    ),
                    // Lista dei livelli
                    Expanded(
                      child: ListView.builder(
                        padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 8),
                        itemCount: structure.length,
                        itemBuilder: (context, index) {
                          return _buildLevelRow(index);
                        },
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
          Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 1800),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    FocusableButton(
                      text: '+ AGGIUNGI LIVELLO',
                      onPressed: _addLevel,
                    ),
                    FocusableButton(
                      text: '▶ AVVIA TIMER',
                      onPressed: _startTimer,
                      autofocus: true,
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLevelRow(int index) {
    final level = structure[index];
    final theme = Theme.of(context);
    final isRowSelected = _selectedRowIndex == index;

    return Focus(
      focusNode: _rowFocusNodes[index],
      onFocusChange: (hasFocus) {
        if (hasFocus) {
          setState(() {
            _selectedRowIndex = index;
            _isEditingRow = false;
            _selectedColumnIndex = 0;
          });
        }
      },
      onKeyEvent: (node, event) {
        if (event is! KeyDownEvent) return KeyEventResult.ignored;

        if (!_isEditingRow) {
          // Modalità selezione riga
          if (event.logicalKey == LogicalKeyboardKey.select ||
              event.logicalKey == LogicalKeyboardKey.enter) {
            setState(() {
              _isEditingRow = true;
              _selectedColumnIndex = 0;
            });
            return KeyEventResult.handled;
          }
        } else {
          // Modalità modifica interna
          if (event.logicalKey == LogicalKeyboardKey.arrowLeft) {
            setState(() {
              _selectedColumnIndex = (_selectedColumnIndex - 1).clamp(0, 2);
            });
            return KeyEventResult.handled;
          } else if (event.logicalKey == LogicalKeyboardKey.arrowRight) {
            setState(() {
              _selectedColumnIndex = (_selectedColumnIndex + 1).clamp(0, 2);
            });
            return KeyEventResult.handled;
          } else if (event.logicalKey == LogicalKeyboardKey.select ||
              event.logicalKey == LogicalKeyboardKey.enter) {
            // Esegui azione in base a quale colonna è selezionata
            if (_selectedColumnIndex == 0) {
              _editSmallBlind(index);
            } else if (_selectedColumnIndex == 1) {
              _editDuration(index);
            } else if (_selectedColumnIndex == 2) {
              _deleteLevel(index);
            }
            return KeyEventResult.handled;
          } else if (event.logicalKey == LogicalKeyboardKey.goBack ||
              event.logicalKey == LogicalKeyboardKey.escape) {
            setState(() {
              _isEditingRow = false;
              _selectedColumnIndex = 0;
            });
            return KeyEventResult.handled;
          }
        }

        return KeyEventResult.ignored;
      },
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 20),
        decoration: BoxDecoration(
          color: isRowSelected
              ? theme.colorScheme.primary.withOpacity(0.08)
              : Colors.transparent,
          border: Border(
            bottom: BorderSide(
              color: theme.colorScheme.onSurface.withOpacity(0.1),
              width: 1,
            ),
          ),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Livello
            Expanded(
              flex: 2,
              child: Text(
                '${level.levelNumber}',
                style: const TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            // Small Blind
            Expanded(
              flex: 3,
              child: Text(
                '${level.smallBlind}',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: (isRowSelected && _isEditingRow && _selectedColumnIndex == 0)
                      ? FontWeight.bold
                      : FontWeight.normal,
                  color: (isRowSelected && _isEditingRow && _selectedColumnIndex == 0)
                      ? Colors.amber
                      : isRowSelected
                          ? theme.colorScheme.primary
                          : theme.colorScheme.onSurface.withOpacity(0.8),
                ),
              ),
            ),
            // Big Blind
            Expanded(
              flex: 3,
              child: Text(
                '${level.bigBlind}',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.normal,
                  color: isRowSelected
                      ? theme.colorScheme.primary
                      : theme.colorScheme.onSurface.withOpacity(0.5),
                ),
              ),
            ),
            // Durata
            Expanded(
              flex: 3,
              child: Text(
                '${level.durationMinutes} min',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: (isRowSelected && _isEditingRow && _selectedColumnIndex == 1)
                      ? FontWeight.bold
                      : FontWeight.normal,
                  color: (isRowSelected && _isEditingRow && _selectedColumnIndex == 1)
                      ? Colors.amber
                      : isRowSelected
                          ? theme.colorScheme.primary
                          : theme.colorScheme.onSurface.withOpacity(0.8),
                ),
              ),
            ),
            // Elimina - Icona cestino
            Expanded(
              flex: 2,
              child: Align(
                alignment: Alignment.centerRight,
                child: Icon(
                  Icons.delete_outline,
                  size: 28,
                  color: (isRowSelected && _isEditingRow && _selectedColumnIndex == 2)
                      ? Colors.red
                      : Colors.red.withOpacity(0.4),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildValueColumn({
    required String label,
    required String value,
    required bool isSelected,
    required bool rowHasFocus,
    bool isEditable = true,
  }) {
    final theme = Theme.of(context);

    Color valueColor;
    if (isSelected) {
      valueColor = Colors.amber;
    } else if (rowHasFocus) {
      valueColor = theme.colorScheme.primary;
    } else {
      valueColor = theme.colorScheme.onSurface.withOpacity(isEditable ? 0.8 : 0.5);
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          label,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.bold,
            color: theme.colorScheme.onSurface.withOpacity(0.5),
          ),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: TextStyle(
            fontSize: 20,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
            color: valueColor,
          ),
        ),
      ],
    );
  }
}
