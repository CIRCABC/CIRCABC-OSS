
module.exports = {
  compress: (gulp) => () => {
    const imagemin = require('gulp-imagemin');
    return gulp.src('src/img/**')
        .pipe(imagemin())
        .pipe(gulp.dest('dist/img'));
  }
};
