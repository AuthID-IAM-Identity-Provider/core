module.exports = {
  // Start with the recommended conventional-changelog rules
  extends: ['@commitlint/config-conventional'],

  // Add your own custom rules
  rules: {
    // Level [0..2]: 0 = disable, 1 = warning, 2 = error
    // Applicable: always | never
    // Value: any value

    // Enforce that the 'type' is always one of the types listed in your CONTRIBUTING.md
    'type-enum': [
      2, // Level: error
      'always', // Applicable
      [    // Allowed values
        'feat',
        'fix',
        'docs',
        'style',
        'refactor',
        'perf',
        'test',
        'chore',
      ],
    ],
    // You can add other rules here if needed, for example:
    'scope-case': [2, 'always', 'kebab-case'], // Enforce kebab-case for scopes
    'subject-case': [2, 'always', 'lower-case'], // Enforce lower-case for the subject
  },
};
