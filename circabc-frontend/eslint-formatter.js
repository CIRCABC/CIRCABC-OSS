/**
 * Custom ESLint Formatter that shows only file paths and line/column numbers.
 */
/*
module.exports = function(results) {
    // Return a string of the file paths and line/column numbers of all issues
    return results
      .map(result => 
        result.messages
          .map(msg => `${result.filePath}(${msg.line},${msg.column})`)
          .join('\n')
      )
      .join('\n');
  };
  
*/
  const path = require('path');

/**
 * Custom ESLint Formatter that shows only relative file paths and line/column numbers.
 */
module.exports = function(results) {
  return results
    .map(result => {
      const relativePath = path.relative(process.cwd(), result.filePath); // Convert to relative path
      return result.messages
        .map(msg => `${relativePath}(${msg.line},${msg.column})`)
        .join('\n');
    })
    .join('\n');
};
