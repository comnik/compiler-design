// Generated from ../src/cd/frontend/parser/Javali.g4 by ANTLR 4.4

	// Java header
	package cd.frontend.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JavaliLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__38=1, T__37=2, T__36=3, T__35=4, T__34=5, T__33=6, T__32=7, T__31=8, 
		T__30=9, T__29=10, T__28=11, T__27=12, T__26=13, T__25=14, T__24=15, T__23=16, 
		T__22=17, T__21=18, T__20=19, T__19=20, T__18=21, T__17=22, T__16=23, 
		T__15=24, T__14=25, T__13=26, T__12=27, T__11=28, T__10=29, T__9=30, T__8=31, 
		T__7=32, T__6=33, T__5=34, T__4=35, T__3=36, T__2=37, T__1=38, T__0=39, 
		Identifier=40, Integer=41, Boolean=42, COMMENT=43, LINE_COMMENT=44, WS=45;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] tokenNames = {
		"'\\u0000'", "'\\u0001'", "'\\u0002'", "'\\u0003'", "'\\u0004'", "'\\u0005'", 
		"'\\u0006'", "'\\u0007'", "'\b'", "'\t'", "'\n'", "'\\u000B'", "'\f'", 
		"'\r'", "'\\u000E'", "'\\u000F'", "'\\u0010'", "'\\u0011'", "'\\u0012'", 
		"'\\u0013'", "'\\u0014'", "'\\u0015'", "'\\u0016'", "'\\u0017'", "'\\u0018'", 
		"'\\u0019'", "'\\u001A'", "'\\u001B'", "'\\u001C'", "'\\u001D'", "'\\u001E'", 
		"'\\u001F'", "' '", "'!'", "'\"'", "'#'", "'$'", "'%'", "'&'", "'''", 
		"'('", "')'", "'*'", "'+'", "','", "'-'"
	};
	public static final String[] ruleNames = {
		"T__38", "T__37", "T__36", "T__35", "T__34", "T__33", "T__32", "T__31", 
		"T__30", "T__29", "T__28", "T__27", "T__26", "T__25", "T__24", "T__23", 
		"T__22", "T__21", "T__20", "T__19", "T__18", "T__17", "T__16", "T__15", 
		"T__14", "T__13", "T__12", "T__11", "T__10", "T__9", "T__8", "T__7", "T__6", 
		"T__5", "T__4", "T__3", "T__2", "T__1", "T__0", "Identifier", "Integer", 
		"Decimal", "Digit", "Hex", "HexDigit", "Boolean", "Letter", "JavaIDDigit", 
		"COMMENT", "LINE_COMMENT", "WS"
	};


	public JavaliLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Javali.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2/\u0149\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t"+
		"\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\21\3\22\3\22\3\22"+
		"\3\22\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\26\3\26"+
		"\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\32"+
		"\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3"+
		"#\3$\3$\3$\3$\3$\3%\3%\3%\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3)\7)\u00f8"+
		"\n)\f)\16)\u00fb\13)\3*\3*\5*\u00ff\n*\3+\3+\3+\7+\u0104\n+\f+\16+\u0107"+
		"\13+\5+\u0109\n+\3,\3,\3-\3-\3-\3-\5-\u0111\n-\3-\3-\7-\u0115\n-\f-\16"+
		"-\u0118\13-\3.\3.\5.\u011c\n.\3/\3/\3/\3/\3/\3/\3/\3/\3/\5/\u0127\n/\3"+
		"\60\3\60\3\61\3\61\3\62\3\62\3\62\3\62\7\62\u0131\n\62\f\62\16\62\u0134"+
		"\13\62\3\62\3\62\3\62\3\62\3\62\3\63\3\63\3\63\3\63\7\63\u013f\n\63\f"+
		"\63\16\63\u0142\13\63\3\63\3\63\3\64\3\64\3\64\3\64\3\u0132\2\65\3\3\5"+
		"\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21"+
		"!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!"+
		"A\"C#E$G%I&K\'M(O)Q*S+U\2W\2Y\2[\2],_\2a\2c-e.g/\3\2\6\16\2&&C\\aac|\u00c2"+
		"\u00d8\u00da\u00f8\u00fa\u2001\u3042\u3191\u3302\u3381\u3402\u3d2f\u4e02"+
		"\ua001\uf902\ufb01\21\2\62;\u0662\u066b\u06f2\u06fb\u0968\u0971\u09e8"+
		"\u09f1\u0a68\u0a71\u0ae8\u0af1\u0b68\u0b71\u0be9\u0bf1\u0c68\u0c71\u0ce8"+
		"\u0cf1\u0d68\u0d71\u0e52\u0e5b\u0ed2\u0edb\u1042\u104b\4\2\f\f\17\17\5"+
		"\2\13\f\16\17\"\"\u014d\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2"+
		"\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25"+
		"\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2"+
		"\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2"+
		"\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3"+
		"\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2"+
		"\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2"+
		"Q\3\2\2\2\2S\3\2\2\2\2]\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\3i\3"+
		"\2\2\2\5k\3\2\2\2\7o\3\2\2\2\tv\3\2\2\2\13y\3\2\2\2\r\177\3\2\2\2\17\u0082"+
		"\3\2\2\2\21\u0088\3\2\2\2\23\u008d\3\2\2\2\25\u008f\3\2\2\2\27\u0091\3"+
		"\2\2\2\31\u0094\3\2\2\2\33\u0096\3\2\2\2\35\u0098\3\2\2\2\37\u00a0\3\2"+
		"\2\2!\u00a3\3\2\2\2#\u00a6\3\2\2\2%\u00aa\3\2\2\2\'\u00af\3\2\2\2)\u00b1"+
		"\3\2\2\2+\u00b3\3\2\2\2-\u00b8\3\2\2\2/\u00ba\3\2\2\2\61\u00bc\3\2\2\2"+
		"\63\u00c4\3\2\2\2\65\u00ca\3\2\2\2\67\u00cd\3\2\2\29\u00d5\3\2\2\2;\u00d7"+
		"\3\2\2\2=\u00d9\3\2\2\2?\u00dc\3\2\2\2A\u00de\3\2\2\2C\u00e0\3\2\2\2E"+
		"\u00e2\3\2\2\2G\u00e4\3\2\2\2I\u00e9\3\2\2\2K\u00ee\3\2\2\2M\u00f0\3\2"+
		"\2\2O\u00f2\3\2\2\2Q\u00f4\3\2\2\2S\u00fe\3\2\2\2U\u0108\3\2\2\2W\u010a"+
		"\3\2\2\2Y\u0110\3\2\2\2[\u011b\3\2\2\2]\u0126\3\2\2\2_\u0128\3\2\2\2a"+
		"\u012a\3\2\2\2c\u012c\3\2\2\2e\u013a\3\2\2\2g\u0145\3\2\2\2ij\7\61\2\2"+
		"j\4\3\2\2\2kl\7p\2\2lm\7g\2\2mn\7y\2\2n\6\3\2\2\2op\7t\2\2pq\7g\2\2qr"+
		"\7v\2\2rs\7w\2\2st\7t\2\2tu\7p\2\2u\b\3\2\2\2vw\7#\2\2wx\7?\2\2x\n\3\2"+
		"\2\2yz\7e\2\2z{\7n\2\2{|\7c\2\2|}\7u\2\2}~\7u\2\2~\f\3\2\2\2\177\u0080"+
		"\7~\2\2\u0080\u0081\7~\2\2\u0081\16\3\2\2\2\u0082\u0083\7y\2\2\u0083\u0084"+
		"\7j\2\2\u0084\u0085\7k\2\2\u0085\u0086\7n\2\2\u0086\u0087\7g\2\2\u0087"+
		"\20\3\2\2\2\u0088\u0089\7x\2\2\u0089\u008a\7q\2\2\u008a\u008b\7k\2\2\u008b"+
		"\u008c\7f\2\2\u008c\22\3\2\2\2\u008d\u008e\7=\2\2\u008e\24\3\2\2\2\u008f"+
		"\u0090\7}\2\2\u0090\26\3\2\2\2\u0091\u0092\7(\2\2\u0092\u0093\7(\2\2\u0093"+
		"\30\3\2\2\2\u0094\u0095\7?\2\2\u0095\32\3\2\2\2\u0096\u0097\7\177\2\2"+
		"\u0097\34\3\2\2\2\u0098\u0099\7g\2\2\u0099\u009a\7z\2\2\u009a\u009b\7"+
		"v\2\2\u009b\u009c\7g\2\2\u009c\u009d\7p\2\2\u009d\u009e\7f\2\2\u009e\u009f"+
		"\7u\2\2\u009f\36\3\2\2\2\u00a0\u00a1\7k\2\2\u00a1\u00a2\7h\2\2\u00a2 "+
		"\3\2\2\2\u00a3\u00a4\7>\2\2\u00a4\u00a5\7?\2\2\u00a5\"\3\2\2\2\u00a6\u00a7"+
		"\7k\2\2\u00a7\u00a8\7p\2\2\u00a8\u00a9\7v\2\2\u00a9$\3\2\2\2\u00aa\u00ab"+
		"\7p\2\2\u00ab\u00ac\7w\2\2\u00ac\u00ad\7n\2\2\u00ad\u00ae\7n\2\2\u00ae"+
		"&\3\2\2\2\u00af\u00b0\7*\2\2\u00b0(\3\2\2\2\u00b1\u00b2\7,\2\2\u00b2*"+
		"\3\2\2\2\u00b3\u00b4\7v\2\2\u00b4\u00b5\7j\2\2\u00b5\u00b6\7k\2\2\u00b6"+
		"\u00b7\7u\2\2\u00b7,\3\2\2\2\u00b8\u00b9\7.\2\2\u00b9.\3\2\2\2\u00ba\u00bb"+
		"\7\60\2\2\u00bb\60\3\2\2\2\u00bc\u00bd\7d\2\2\u00bd\u00be\7q\2\2\u00be"+
		"\u00bf\7q\2\2\u00bf\u00c0\7n\2\2\u00c0\u00c1\7g\2\2\u00c1\u00c2\7c\2\2"+
		"\u00c2\u00c3\7p\2\2\u00c3\62\3\2\2\2\u00c4\u00c5\7y\2\2\u00c5\u00c6\7"+
		"t\2\2\u00c6\u00c7\7k\2\2\u00c7\u00c8\7v\2\2\u00c8\u00c9\7g\2\2\u00c9\64"+
		"\3\2\2\2\u00ca\u00cb\7@\2\2\u00cb\u00cc\7?\2\2\u00cc\66\3\2\2\2\u00cd"+
		"\u00ce\7y\2\2\u00ce\u00cf\7t\2\2\u00cf\u00d0\7k\2\2\u00d0\u00d1\7v\2\2"+
		"\u00d1\u00d2\7g\2\2\u00d2\u00d3\7n\2\2\u00d3\u00d4\7p\2\2\u00d48\3\2\2"+
		"\2\u00d5\u00d6\7]\2\2\u00d6:\3\2\2\2\u00d7\u00d8\7>\2\2\u00d8<\3\2\2\2"+
		"\u00d9\u00da\7?\2\2\u00da\u00db\7?\2\2\u00db>\3\2\2\2\u00dc\u00dd\7_\2"+
		"\2\u00dd@\3\2\2\2\u00de\u00df\7@\2\2\u00dfB\3\2\2\2\u00e0\u00e1\7#\2\2"+
		"\u00e1D\3\2\2\2\u00e2\u00e3\7\'\2\2\u00e3F\3\2\2\2\u00e4\u00e5\7t\2\2"+
		"\u00e5\u00e6\7g\2\2\u00e6\u00e7\7c\2\2\u00e7\u00e8\7f\2\2\u00e8H\3\2\2"+
		"\2\u00e9\u00ea\7g\2\2\u00ea\u00eb\7n\2\2\u00eb\u00ec\7u\2\2\u00ec\u00ed"+
		"\7g\2\2\u00edJ\3\2\2\2\u00ee\u00ef\7+\2\2\u00efL\3\2\2\2\u00f0\u00f1\7"+
		"-\2\2\u00f1N\3\2\2\2\u00f2\u00f3\7/\2\2\u00f3P\3\2\2\2\u00f4\u00f9\5_"+
		"\60\2\u00f5\u00f8\5_\60\2\u00f6\u00f8\5a\61\2\u00f7\u00f5\3\2\2\2\u00f7"+
		"\u00f6\3\2\2\2\u00f8\u00fb\3\2\2\2\u00f9\u00f7\3\2\2\2\u00f9\u00fa\3\2"+
		"\2\2\u00faR\3\2\2\2\u00fb\u00f9\3\2\2\2\u00fc\u00ff\5U+\2\u00fd\u00ff"+
		"\5Y-\2\u00fe\u00fc\3\2\2\2\u00fe\u00fd\3\2\2\2\u00ffT\3\2\2\2\u0100\u0109"+
		"\7\62\2\2\u0101\u0105\4\63;\2\u0102\u0104\5W,\2\u0103\u0102\3\2\2\2\u0104"+
		"\u0107\3\2\2\2\u0105\u0103\3\2\2\2\u0105\u0106\3\2\2\2\u0106\u0109\3\2"+
		"\2\2\u0107\u0105\3\2\2\2\u0108\u0100\3\2\2\2\u0108\u0101\3\2\2\2\u0109"+
		"V\3\2\2\2\u010a\u010b\4\62;\2\u010bX\3\2\2\2\u010c\u010d\7\62\2\2\u010d"+
		"\u0111\7z\2\2\u010e\u010f\7\62\2\2\u010f\u0111\7Z\2\2\u0110\u010c\3\2"+
		"\2\2\u0110\u010e\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0116\5[.\2\u0113\u0115"+
		"\5[.\2\u0114\u0113\3\2\2\2\u0115\u0118\3\2\2\2\u0116\u0114\3\2\2\2\u0116"+
		"\u0117\3\2\2\2\u0117Z\3\2\2\2\u0118\u0116\3\2\2\2\u0119\u011c\5_\60\2"+
		"\u011a\u011c\5a\61\2\u011b\u0119\3\2\2\2\u011b\u011a\3\2\2\2\u011c\\\3"+
		"\2\2\2\u011d\u011e\7v\2\2\u011e\u011f\7t\2\2\u011f\u0120\7w\2\2\u0120"+
		"\u0127\7g\2\2\u0121\u0122\7h\2\2\u0122\u0123\7c\2\2\u0123\u0124\7n\2\2"+
		"\u0124\u0125\7u\2\2\u0125\u0127\7g\2\2\u0126\u011d\3\2\2\2\u0126\u0121"+
		"\3\2\2\2\u0127^\3\2\2\2\u0128\u0129\t\2\2\2\u0129`\3\2\2\2\u012a\u012b"+
		"\t\3\2\2\u012bb\3\2\2\2\u012c\u012d\7\61\2\2\u012d\u012e\7,\2\2\u012e"+
		"\u0132\3\2\2\2\u012f\u0131\13\2\2\2\u0130\u012f\3\2\2\2\u0131\u0134\3"+
		"\2\2\2\u0132\u0133\3\2\2\2\u0132\u0130\3\2\2\2\u0133\u0135\3\2\2\2\u0134"+
		"\u0132\3\2\2\2\u0135\u0136\7,\2\2\u0136\u0137\7\61\2\2\u0137\u0138\3\2"+
		"\2\2\u0138\u0139\b\62\2\2\u0139d\3\2\2\2\u013a\u013b\7\61\2\2\u013b\u013c"+
		"\7\61\2\2\u013c\u0140\3\2\2\2\u013d\u013f\n\4\2\2\u013e\u013d\3\2\2\2"+
		"\u013f\u0142\3\2\2\2\u0140\u013e\3\2\2\2\u0140\u0141\3\2\2\2\u0141\u0143"+
		"\3\2\2\2\u0142\u0140\3\2\2\2\u0143\u0144\b\63\2\2\u0144f\3\2\2\2\u0145"+
		"\u0146\t\5\2\2\u0146\u0147\3\2\2\2\u0147\u0148\b\64\2\2\u0148h\3\2\2\2"+
		"\16\2\u00f7\u00f9\u00fe\u0105\u0108\u0110\u0116\u011b\u0126\u0132\u0140"+
		"\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}