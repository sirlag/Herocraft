/*
 * ANTLR 4 Grammar for the Herocraft Query Language
 * Supports:
 * - Implicit and Explicit AND
 * - Explicit OR, NOT
 * - Parentheses for grouping
 * - Field queries with colon (field:value)
 * - Field queries with comparison operators (field > value, field <= value etc.)
 * - WORD (unquoted) and QUOTED_STRING values
 * Generated: 2025-04-11 // Adjusted to current date provided in context
 */
grammar HQL;

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/

// Entry point: An entire query is a single expression until end-of-file.
query
    : expression EOF
    ;

// Base rule for expressions, setting up precedence.
expression
    : orExpression
    ;

// Handles OR operations (lowest precedence).
orExpression
    : andExpression (OR andExpression)*
    ;

// Handles AND operations (higher precedence than OR).
// Connects one or more primaryExpressions. Adjacent primaryExpressions imply AND.
// The 'AND' keyword can optionally appear between expressions without changing the logic.
andExpression
    : primaryExpression (AND? primaryExpression)*
    ;

// Handles individual terms, negation, and parenthesized groups (highest precedence).
primaryExpression
    : NOT? (term | LPAREN expression RPAREN)
    ;

// Represents a single searchable item. Can be:
// 1. A field-based query (field:value or field > value)
// 2. A simple value query
term
    : fieldSpecifier COLON atomicValue
    | fieldSpecifier comparisonOperator atomicValue // Starts with WORD followed by : or operator
    | simpleTerm                     // Is just a WORD or QUOTED_STRING value
    ;


// The identifier used for a field query (interpreted by the application).
fieldSpecifier
    : WORD
    ;

// Groups the comparison operators.
comparisonOperator
    : GT | LT | EQ | GTE | LTE | NEQ
    ;

// A simple term query (not associated with a specific field).
simpleTerm
    : atomicValue
    ;

// The value being searched for.
atomicValue
    : WORD
    | QUOTED_STRING
    ;


/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

// -- Boolean Operators --
// IMPORTANT: Define keywords *before* the generic WORD rule.
AND           : 'AND';
OR            : 'OR';
NOT           : 'NOT';

// -- Comparison Operators --
// IMPORTANT: Define multi-character operators (>=, <=, !=) *before*
// single-character ones (>, <, =) to ensure correct longest match.
GTE           : '>=';
LTE           : '<=';
NEQ           : '!=';
GT            : '>';
LT            : '<';
EQ            : '=';

// -- Structural Characters --
LPAREN        : '(';
RPAREN        : ')';
COLON         : ':'; // Separator for standard field queries

// -- Values --
// Basic unquoted query terms/field names.
// IMPORTANT: Must come AFTER specific keywords (AND, OR, NOT) in the grammar file.
WORD          : [a-zA-Z0-9_/-]+ ; // Adjust character set if needed

// Quoted strings to allow spaces, special chars, etc. Handles escaped quotes (\").
QUOTED_STRING : '"' ( ('\\"') | ~["] )*? '"'; // Non-greedy match

// -- Whitespace --
// Skip whitespace characters (spaces, tabs, newlines).
WS            : [ \t\r\n]+ -> skip;