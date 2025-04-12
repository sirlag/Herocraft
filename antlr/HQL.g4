/*
 * ANTLR 4 Grammar for the Herocraft Query Language (HQL)
 * Supports:
 * - Implicit and Explicit AND
 * - Explicit OR
 * - Explicit NOT and prefix '-' for negation
 * - Parentheses for grouping
 * - Field queries with colon (field:value)
 * - Field queries with comparison operators (field > value, field <= value etc.)
 * - WORD (unquoted) and QUOTED_STRING values
 * Last Updated: 2025-04-12 // Use current date
 */
grammar HQL; // Using the name from your file

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

// Handles highest precedence: negation, basic terms, or parenthesized expressions.
// Separates negation+baseTerm from parentheses for potentially clearer parsing.
primaryExpression
    : negationOperator? baseTerm         // A potentially negated base term
    | LPAREN expression RPAREN           // Or a parenthesized expression
    ;

// Defines the allowed negation operators.
negationOperator
    : NOT | NEGATE // Allow either 'NOT' keyword or '-' symbol
    ;

// Represents the core part of a term, without negation. This replaces the old 'term' structure.
baseTerm
    : fieldSpecifier comparisonOperator atomicValue // Starts with WORD followed by : or operator
    | simpleTerm                     // Is just a WORD or QUOTED_STRING value
    ;

// The identifier used for a field query (interpreted by the application).
fieldSpecifier
    : WORD
    ;

// Groups the comparison operators.
comparisonOperator
    : GT | LT | EQ | GTE | LTE | NEQ | IS
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

// -- Boolean/Negation Operators --
// IMPORTANT: Define keywords/operators *before* the generic WORD rule.
AND           : 'AND';
OR            : 'OR';
NOT           : 'NOT';
NEGATE        : '-';   // Negation operator token

// -- Comparison Operators --
// IMPORTANT: Define multi-character operators (>=, <=, !=) *before*
// single-character ones (>, <, =) to ensure correct longest match.
GTE           : '>=';
LTE           : '<=';
NEQ           : '!=';
GT            : '>';
LT            : '<';
EQ            : '=';
IS            : ':';

// -- Structural Characters --
LPAREN        : '(';
RPAREN        : ')';

// -- Values --
// Basic unquoted query terms/field names. Allows internal hyphens.
// IMPORTANT: Must come AFTER specific keywords/operators (AND, OR, NOT, NEGATE, operators)
//            in the grammar file for correct tokenization.
WORD : [a-zA-Z0-9_/] [a-zA-Z0-9_/-]* ; // First char cannot be '-', subsequent can.

// Quoted strings to allow spaces, special chars, etc. Handles escaped quotes (\").
QUOTED_STRING : '"' ( ('\\"') | ~["] )*? '"'; // Non-greedy match

// -- Whitespace --
// Skip whitespace characters (spaces, tabs, newlines).
WS            : [ \t\r\n]+ -> skip;