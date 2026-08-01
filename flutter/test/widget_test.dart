import 'package:flutter_test/flutter_test.dart';

import 'package:find_campusflutter/main.dart';

void main() {
  testWidgets('FindCampus home renders', (WidgetTester tester) async {
    await tester.pumpWidget(const FindCampusApp());

    expect(find.text('FindCampus'), findsOneWidget);
  });
}
