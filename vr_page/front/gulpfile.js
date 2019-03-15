const gulp = require("gulp");
const browserify = require("browserify");
const fs = require("fs");

gulp.task("build", () => {
    const out = fs.createWriteStream('./out.js');

    return browserify({
        entries:['index_20190214.js']
    })
    .bundle()
    .pipe(out);
});

gulp.task("watch", () =>{
    return gulp.watch('./index_20190214.js', gulp.task("build"));
});
