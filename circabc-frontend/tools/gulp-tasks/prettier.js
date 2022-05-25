// clang-format entry points
const srcsToFmt = [
  'src/**/*.{js,ts}',
  '!src/app/core/generated/**/*.ts'
];

const scsssToFmt = ['src/**/*.scss'];

const htmlsToFmt = ['src/**/*.html'];

module.exports = {
  format: gulp => () => {
    const prettier = require('gulp-prettier');
    return gulp
      .src(srcsToFmt, { base: '.' })
      .pipe(prettier({ singleQuote: true }))
      .pipe(gulp.dest('.'));
  },
  scss: gulp => () => {
    const prettier = require('gulp-prettier');
    return gulp
      .src(scsssToFmt, { base: '.' })
      .pipe(prettier({ singleQuote: true }))
      .pipe(gulp.dest('.'));
  },

  html: gulp => () => {
    const prettier = require('gulp-prettier');
    return gulp
      .src(htmlsToFmt, { base: '.' })
      .pipe(prettier({ singleQuote: true }))
      .pipe(gulp.dest('.'));
  }
};
