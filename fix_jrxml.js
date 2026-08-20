const fs = require('fs');
const p = 'C:/Users/TSI-KEN04/Documents/GitHub/noreco1-firefly-v2/src/main/resources/jasper/vouchers/CostEstimate.jrxml';
let c = fs.readFileSync(p, 'utf8');

// 1. Move GRAND TOTAL label y=154 -> y=173
c = c.replace(
  'x="133" y="154" width="251" height="20" isRemoveLineWhenBlank="true" forecolor="#000000" backcolor="#FFFFFF" uuid="5cd6ae44-034b-46fb-b01a-d92518f0f7f6"',
  'x="133" y="173" width="251" height="20" isRemoveLineWhenBlank="true" forecolor="#000000" backcolor="#FFFFFF" uuid="5cd6ae44-034b-46fb-b01a-d92518f0f7f6"'
);

// 2. Move GRAND TOTAL amount y=154 -> y=173
c = c.replace(
  'x="384" y="154" width="120" height="20" isRemoveLineWhenBlank="true" forecolor="#000000" backcolor="#FFFFFF" uuid="fc5f547c-15be-47a3-b003-e224e584760f"',
  'x="384" y="173" width="120" height="20" isRemoveLineWhenBlank="true" forecolor="#000000" backcolor="#FFFFFF" uuid="fc5f547c-15be-47a3-b003-e224e584760f"'
);

// 3. Add CONTINGENCY to Grand Total formula
const oldFormula = '$P{TOTAL_MATERIAL_COST}.add($P{TOTAL_LABOR_COST}).add($P{FREIGHT_HANDLING}).add($P{TOTAL_MISCELLANEOUS_CHARGE}).add($P{TOTAL_ASSEMBLY_LABOR_COST}).add($P{TOTAL_METERING_COST})';
const newFormula = oldFormula + '.add($P{CONTINGENCY})';
if (!c.includes(oldFormula)) { console.error('Formula not found!'); process.exit(1); }
c = c.replace(oldFormula, newFormula);

// 4. Build CONTINGENCY block
const R = '\r\n';
const T4 = '\t\t\t\t';
const T5 = '\t\t\t\t\t';
const T6 = '\t\t\t\t\t\t';
const block =
  T4 + '<staticText>' + R +
  T5 + '<reportElement positionType="Float" mode="Transparent" x="133" y="153" width="251" height="20" isRemoveLineWhenBlank="true" forecolor="#000000" backcolor="#FFFFFF" uuid="a1b2c3d4-e5f6-7890-abcd-ef1234567890">' + R +
  T6 + '<printWhenExpression><![CDATA[new Boolean($P{CONTINGENCY}.compareTo(BigDecimal.ZERO) != 0)]]></printWhenExpression>' + R +
  T5 + '</reportElement>' + R +
  T5 + '<box rightPadding="5"/>' + R +
  T5 + '<textElement textAlignment="Right" verticalAlignment="Middle" rotation="None" markup="none">' + R +
  T6 + '<font fontName="SansSerif" size="12" isBold="true" isItalic="false" isUnderline="false" isStrikeThrough="false" pdfEncoding="Cp1252" isPdfEmbedded="false"/>' + R +
  T6 + '<paragraph lineSpacing="Single"/>' + R +
  T5 + '</textElement>' + R +
  T5 + '<text><![CDATA[CONTINGENCY]]></text>' + R +
  T4 + '</staticText>' + R +
  T4 + '<textField isBlankWhenNull="true">' + R +
  T5 + '<reportElement positionType="Float" mode="Transparent" x="384" y="153" width="34" height="20" isRemoveLineWhenBlank="true" forecolor="#000000" backcolor="#FFFFFF" uuid="b2c3d4e5-f6a7-8901-bcde-f12345678901">' + R +
  T6 + '<printWhenExpression><![CDATA[new Boolean($P{CONTINGENCY}.compareTo(BigDecimal.ZERO) != 0)]]></printWhenExpression>' + R +
  T5 + '</reportElement>' + R +
  T5 + '<box rightPadding="5"/>' + R +
  T5 + '<textElement textAlignment="Left" verticalAlignment="Middle" rotation="None" markup="none">' + R +
  T6 + '<font fontName="SansSerif" size="11" isBold="true" isItalic="false" isUnderline="false" isStrikeThrough="false" pdfEncoding="Cp1252" isPdfEmbedded="false"/>' + R +
  T6 + '<paragraph lineSpacing="Single"/>' + R +
  T5 + '</textElement>' + R +
  T5 + '<textFieldExpression><![CDATA[$P{CONTINGENCY_PERCENTAGE}]]></textFieldExpression>' + R +
  T4 + '</textField>' + R +
  T4 + '<textField pattern="#,##0.00;(#,##0.00)" isBlankWhenNull="true">' + R +
  T5 + '<reportElement positionType="Float" mode="Transparent" x="416" y="153" width="88" height="20" isRemoveLineWhenBlank="true" forecolor="#000000" backcolor="#FFFFFF" uuid="c3d4e5f6-a7b8-9012-cdef-123456789012">' + R +
  T6 + '<printWhenExpression><![CDATA[new Boolean($P{CONTINGENCY}.compareTo(BigDecimal.ZERO) != 0)]]></printWhenExpression>' + R +
  T5 + '</reportElement>' + R +
  T5 + '<box rightPadding="5"/>' + R +
  T5 + '<textElement textAlignment="Right" verticalAlignment="Middle" rotation="None" markup="none">' + R +
  T6 + '<font fontName="SansSerif" size="12" isBold="true" isItalic="false" isUnderline="false" isStrikeThrough="false" pdfEncoding="Cp1252" isPdfEmbedded="false"/>' + R +
  T6 + '<paragraph lineSpacing="Single"/>' + R +
  T5 + '</textElement>' + R +
  T5 + '<textFieldExpression><![CDATA[$P{CONTINGENCY}]]></textFieldExpression>' + R +
  T4 + '</textField>' + R;

// Insert before GRAND TOTAL staticText (now at y=173)
const anchor = T4 + '<staticText>\r\n' + T5 + '<reportElement positionType="Float" mode="Transparent" x="133" y="173"';
if (!c.includes(anchor)) { console.error('Anchor not found!'); process.exit(1); }
c = c.replace(anchor, block + anchor);

fs.writeFileSync(p, c, 'utf8');
console.log('Done. Length:', c.length);
