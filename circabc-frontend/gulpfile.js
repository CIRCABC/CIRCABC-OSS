'use strict';

const gulp = require('gulp');

// See `tools/gulp-tasks/README.md` for information about task loading.
function loadTask(fileName, taskName) {
  const taskModule = require('./tools/gulp-tasks/' + fileName);
  const task = taskName ? taskModule[taskName] : taskModule;
  return task(gulp);
}

gulp.task('format:enforce', loadTask('format', 'enforce'));
gulp.task('format', loadTask('format', 'format'));
gulp.task('format:scss', loadTask('prettier', 'scss'));
gulp.task('prettier', loadTask('prettier', 'format'));
gulp.task('prettier:html', loadTask('prettier', 'html'));
gulp.task('imagemin', loadTask('imagemin', 'compress'));
